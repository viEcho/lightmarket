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

    @Override
    @Transactional
    public NonceVO generateNonce(String walletAddress, Integer chainId) {
        if (validAddress(walletAddress)) {
            throw new IllegalArgumentException("Invalid wallet address format");
        }

        // Mark old unused nonce as invalid
        QueryWrapper<UserNonce> invalidateWrapper = new QueryWrapper<>();
        invalidateWrapper.eq("walletAddress", walletAddress)
                .eq("chainId", chainId)
                .eq("used", 0);
        UserNonce updateNonce = new UserNonce();
        updateNonce.setUsed((byte) 1);
        userNonceMapper.update(updateNonce, invalidateWrapper);

        // Generate new nonce
        String nonceValue = UUID.randomUUID().toString().replace("-", "");
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(NONCE_EXPIRATION_MINUTES);

        UserNonce userNonce = new UserNonce();
        userNonce.setWalletAddress(walletAddress);
        userNonce.setChainId(chainId);
        userNonce.setNonce(nonceValue);
        userNonce.setExpiredAt(Date.from(expiredAt.atZone(ZoneId.systemDefault()).toInstant()));
        userNonce.setUsed((byte) 0);
        userNonce.setCreatedTime(new Date());

        userNonceMapper.insert(userNonce);

        // Create message to sign
        String message = buildSignMessage(walletAddress, nonceValue, expiredAt);

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
        nonceWrapper.eq("walletAddress", walletAddress)
                .eq("chainId", chainId)
                .eq("used", 0)
                .orderByDesc("createdTime")
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
        boolean isValid = verifySignature(walletAddress, userNonce.getNonce(), signature);
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
    private String buildSignMessage(String walletAddress, String nonce, LocalDateTime expiredAt) {
        return String.format(
                "Welcome to Light Market!\n\n" +
                        "Please sign this message to verify your wallet ownership.\n" +
                        "This will not cost any gas fees.\n\n" +
                        "Wallet address: %s\n" +
                        "Nonce: %s\n" +
                        "Expires at: %s",
                walletAddress,
                nonce,
                expiredAt.toString()
        );
    }

    /**
     * Verify Ethereum signature
     */
    private boolean verifySignature(String address, String nonce, String signature) {
        try {
            // Get signature data
            Sign.SignatureData signatureData = extractSignature(signature);

            // Recover public key from signed message
            BigInteger publicKey = Sign.signedMessageToKey(
                    nonce.getBytes(),
                    signatureData
            );

            if (publicKey == null) {
                log.error("Failed to recover public key from signature");
                return false;
            }

            // Convert public key to address
            String publicKeyHex = Numeric.toHexStringWithPrefix(publicKey);
            String recoveredAddress = "0x" + Keys.getAddress(publicKeyHex);

            log.info("Recovered address: {}, Expected address: {}", recoveredAddress, address);

            return address.equalsIgnoreCase(recoveredAddress);
        } catch (Exception e) {
            log.error("Signature verification failed", e);
            return false;
        }
    }

    /**
     * Extract signature components
     */
    private Sign.SignatureData extractSignature(String signature) {
        byte[] signatureBytes = Numeric.hexStringToByteArray(signature);

        if (signatureBytes.length < 65) {
            throw new IllegalArgumentException("Invalid signature length");
        }

        byte r = signatureBytes[64];
        byte v = (byte) (r < 27 ? r + 27 : r);

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
        walletWrapper.eq("walletAddress", walletAddress)
                .eq("chainId", chainId);

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
