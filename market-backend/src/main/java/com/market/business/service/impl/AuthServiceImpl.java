package com.market.business.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.market.business.query.UserRegisterQuery;
import com.market.business.entity.User;
import com.market.business.entity.UserNonce;
import com.market.business.entity.UserWallet;
import com.market.business.mapper.UserNonceMapper;
import com.market.business.mapper.UserWalletMapper;
import com.market.business.service.AuthService;
import com.market.business.service.UserService;
import com.market.business.vo.LoginVO;
import com.market.business.vo.NonceVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.web3j.crypto.Keys;
import org.web3j.crypto.Sign;
import org.web3j.utils.Numeric;

import java.math.BigInteger;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;
import javax.annotation.Resource;
/**
 * Auth Service Implementation
 */
@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    @Resource
    private UserNonceMapper userNonceMapper;

    @Resource
    private UserWalletMapper userWalletMapper;

    @Resource
    private UserService userService;

    /**
     * 默认nonce过期时间为15分钟
     */
    private static final long NONCE_EXPIRATION_MINUTES = 15;

    /**
     * 日期时间格式化器 - 使用固定格式避免纳秒不一致问题
     */
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss.SSS");

    @Override
    @Transactional
    public NonceVO generateNonce(String walletAddress, Integer chainId) {
        if (validAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address format");
        }

        // Mark old unused nonce as invalid
        QueryWrapper<UserNonce> invalidateWrapper = new QueryWrapper<>();
        invalidateWrapper.eq(UserNonce.WALLET_ADDRESS, walletAddress)
                .eq(UserNonce.CHAIN_ID, chainId)
                .eq(UserNonce.USED, 0);
        UserNonce updateNonce = new UserNonce();
        updateNonce.setUsed((byte) 1);
        userNonceMapper.update(updateNonce, invalidateWrapper);

        // Generate new nonce
        String nonceValue = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(NONCE_EXPIRATION_MINUTES);

        // 格式化过期时间（只保留毫秒，避免纳秒精度问题）
        String formattedExpiredAt = expiredAt.format(DATE_FORMATTER);

        UserNonce userNonce = new UserNonce();
        userNonce.setWalletAddress(walletAddress);
        userNonce.setChainId(chainId);
        userNonce.setNonce(nonceValue);
        userNonce.setExpiredAt(Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant()));
        userNonce.setFormattedExpiredAt(formattedExpiredAt); // 保存格式化的时间字符串
        userNonce.setUsed((byte) 0);
        userNonce.setCreatedTime(new Date());

        userNonceMapper.insert(userNonce);

        // Create message to sign
        String message = buildSignMessage(walletAddress, nonceValue, formattedExpiredAt);

        log.info("Generated nonce for wallet: {}, nonce: {}, expiredAt: {}", walletAddress, nonceValue, expiredAt);
        log.info("Formatted expiredAt: {}", formattedExpiredAt);
        log.info("Message to sign: {}", message);

        return new NonceVO(
                nonceValue,
                expiredAt.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli(),
                message
        );
    }

    @Override
    @Transactional
    public LoginVO verifyAndLogin(UserRegisterQuery request) {
        String walletAddress = request.getWalletAddress();
        Integer chainId = request.getChainId();
        String signature = request.getSignature();

        if (validAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address format");
        }

        // Find unused nonce
        QueryWrapper<UserNonce> nonceWrapper = new QueryWrapper<>();
        nonceWrapper.eq(UserNonce.WALLET_ADDRESS, walletAddress)
                .eq(UserNonce.CHAIN_ID, chainId)
                .eq(UserNonce.USED, 0)
                .orderByDesc(UserNonce.CREATED_TIME)
                .last("LIMIT 1");

        UserNonce userNonce = userNonceMapper.selectOne(nonceWrapper);
        if (userNonce == null) {
            throw new IllegalArgumentException("No valid nonce found for this wallet");
        }

        // Check if nonce is expired
        if (userNonce.getExpiredAt().before(new Date())) {
            throw new IllegalArgumentException("Nonce has expired");
        }

        // Verify signature
        boolean isValid = verifySignature(walletAddress, userNonce.getNonce(), signature, userNonce.getFormattedExpiredAt());
        if (!isValid) {
            throw new IllegalArgumentException("Invalid signature");
        }

        // Mark nonce as used
        userNonce.setUsed((byte) 1);
        userNonceMapper.updateById(userNonce);

        // Find or create user
        User user = findOrCreateUser(walletAddress, chainId, request.getWalletType());

        // Generate JWT token (simplified - you should use a proper JWT library)
        String token = generateToken(user);

        return new LoginVO(
                token,
                user.getUid(),
                user.getNickname(),
                user.getAvatar(),
                walletAddress
        );
    }

    /**
     * Build message for user to sign
     */
    private String buildSignMessage(String walletAddress, String nonce, String formattedExpiredAt) {
        return String.format(
                "Welcome to Light Market!\n\n" +
                        "Please sign this message to verify your wallet ownership.\n" +
                        "This will not cost any gas fees.\n\n" +
                        "Wallet address: %s\n" +
                        "Nonce: %s\n" +
                        "Expires at: %s",
                walletAddress,
                nonce,
                formattedExpiredAt
        );
    }

    /**
     * Verify Ethereum signature
     */
    private boolean verifySignature(String address, String nonce, String signature, String formattedExpiredAt) {
        try {
            // Get signature data
            Sign.SignatureData signatureData = extractSignature(signature);

            // Build the same message that was signed by the user
            String message = buildSignMessage(address, nonce, formattedExpiredAt);

            log.info("Verifying signature for message: {}", message);

            // ethers.js signMessage() already adds the prefix, so we use the raw message
            // Web3j's Sign.signedMessageToKey expects the message WITH prefix already added
            byte[] messageBytes = message.getBytes();
            byte[] prefixedMessage = getEthereumMessagePrefix(messageBytes);

            log.info("Message length: {}, Prefixed message length: {}", messageBytes.length, prefixedMessage.length);
            log.info("Expected address (input): {}", address);

            // Recover public key from signed message
            BigInteger publicKey = Sign.signedMessageToKey(
                    prefixedMessage,
                    signatureData
            );

            if (publicKey == null) {
                log.error("Failed to recover public key from signature");
                return false;
            }

            // Convert public key to address
            String publicKeyHex = Numeric.toHexStringWithPrefix(publicKey);
            String recoveredAddress = "0x" + Keys.getAddress(publicKeyHex);

            log.info("Public key: {}", publicKeyHex);
            log.info("Recovered address: {}, Expected address: {}", recoveredAddress, address);
            log.info("Address match: {}", address.equalsIgnoreCase(recoveredAddress));

            // Normalize both addresses to lowercase for comparison
            String normalizedExpected = address.toLowerCase();
            String normalizedRecovered = recoveredAddress.toLowerCase();

            return normalizedExpected.equals(normalizedRecovered);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    /**
     * Add Ethereum signed message prefix
     * See: https://eth.wiki/json-rpc/API#eth_sign
     */
    private byte[] getEthereumMessagePrefix(byte[] messageBytes) {
        String prefix = "\u0019Ethereum Signed Message:\n" + messageBytes.length;
        byte[] prefixBytes = prefix.getBytes();
        byte[] result = new byte[prefixBytes.length + messageBytes.length];
        System.arraycopy(prefixBytes, 0, result, 0, prefixBytes.length);
        System.arraycopy(messageBytes, 0, result, prefixBytes.length, messageBytes.length);
        return result;
    }

    /**
     * Extract signature components
     */
    private Sign.SignatureData extractSignature(String signature) {
        // Remove 0x prefix if present
        String cleanSignature = signature.startsWith("0x") ? signature.substring(2) : signature;

        byte[] signatureBytes = Numeric.hexStringToByteArray("0x" + cleanSignature);

        log.info("Signature (raw): {}", signature);
        log.info("Signature length: {} bytes", signatureBytes.length);

        if (signatureBytes.length < 65) {
            throw new IllegalArgumentException("Invalid signature length: " + signatureBytes.length);
        }

        // ethers.js returns: r (32 bytes) + s (32 bytes) + v (1 byte)
        // The v value from ethers.js is already correct (27 or 28)
        byte v = signatureBytes[64];
        int vValue = v & 0xFF; // Convert to unsigned int for logging

        log.info("Signature v value (raw): {} (0x{})", vValue, String.format("%02x", v));

        // ethers.js signMessage returns v in [27, 28], which is correct
        // No need to adjust

        return new Sign.SignatureData(
                v,
                java.util.Arrays.copyOfRange(signatureBytes, 0, 32),
                java.util.Arrays.copyOfRange(signatureBytes, 32, 64)
        );
    }

    /**
     * Validate Ethereum address format
     */
    private boolean validAddress(String address) {
        if (address == null || address.isEmpty()) {
            return true;
        }
        return !address.matches("^0x[a-fA-F0-9]{40}$");
    }

    /**
     * Find existing user or create new one
     */
    private User findOrCreateUser(String walletAddress, Integer chainId, String walletType) {
        // Check if wallet exists
        QueryWrapper<UserWallet> walletWrapper = new QueryWrapper<>();
        walletWrapper.eq(UserWallet.WALLET_ADDRESS, walletAddress)
                .eq(UserWallet.CHAIN_ID, chainId);

        UserWallet userWallet = userWalletMapper.selectOne(walletWrapper);

        User user;
        if (userWallet != null) {
            user = userService.getById(userWallet.getUserId());
        } else {
            user = new User();
            user.setUid(UUID.randomUUID().toString().replace("-", ""));
            user.setNickname(walletAddress.substring(0, 6) + "..." + walletAddress.substring(38));
            user.setDeleteFlag((byte) 0);
            user.setCreatedTime(new Date());
            user.setUpdatedTime(new Date());

            userService.save(user);

            // Create wallet record
            userWallet = new UserWallet();
            userWallet.setUserId(user.getId());
            userWallet.setWalletAddress(walletAddress);
            userWallet.setChainId(chainId);
            userWallet.setWalletType(walletType != null ? walletType : "metamask");
            userWallet.setIsPrimary((byte) 1);
            userWallet.setCreatedTime(new Date());
            userWallet.setUpdatedTime(new Date());

            userWalletMapper.insert(userWallet);
        }
        return user;
    }

    /**
     * Generate JWT token for user
     * TODO: Implement proper JWT token generation with JWT library
     */
    private String generateToken(User user) {
        // This is a simplified token generation
        // In production, use a proper JWT library like io.jsonwebtoken:jjwt
        return "jwt_" + user.getUid() + "_" + System.currentTimeMillis();
    }
}
