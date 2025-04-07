import { useEffect } from "react";
import { Route, Routes } from "react-router-dom";
import { HomePage } from "@/pages/HomePage.tsx";
import PATH from "@/constants/path.ts";
import { useAuthStore } from "@/store/useAuthStore";
import { useCurrentUser } from "@/hooks/api/useUser";
import { AuthCallbackPage } from "@/pages/AuthCallbackPage.tsx";

const App = () => {
  const { setUser, setLoading } = useAuthStore();
  const { data: user, isSuccess, isError, isLoading } = useCurrentUser();

  // 인증 상태 처리
  useEffect(() => {
    if (isLoading) {
      setLoading(true);
    } else if (isSuccess && user) {
      setUser(user);
    } else if (isError) {
      setUser(null);
    }
  }, [user, isSuccess, isError, isLoading, setUser, setLoading]);

  return (
    <Routes>
      <Route path={PATH.HOME} element={<HomePage />} />
      <Route path={PATH.AUTH_CALLBACK} element={<AuthCallbackPage />} />
    </Routes>
  );
};

export default App;
