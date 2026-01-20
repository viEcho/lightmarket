import { useRouter } from 'vue-router'

export function useNavigate() {
  const router = useRouter()

  const navigate = (page, param = null) => {
    if (page === 'markets' || page === 'home') {
      router.push('/markets')
    } else if (page === 'market-detail') {
      router.push(`/market/${param}`)
    } else if (page === 'create-market') {
      router.push('/create-market')
    } else if (page === 'admin-dashboard') {
      router.push('/admin/dashboard')
    } else if (page === 'review-market') {
      router.push('/admin/review')
    }
  }

  return {
    navigate
  }
}
