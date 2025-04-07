import { Route, Routes } from "react-router-dom";
import { HomePage } from "@/pages/HomePage.tsx";
import PATH from "@/constants/path.ts";
import { AuthCallbackPage } from "@/pages/AuthCallbackPage.tsx";

const App = () => {
  return (
    <Routes>
      <Route path={PATH.HOME} element={<HomePage />} />
      <Route path={PATH.AUTH_CALLBACK} element={<AuthCallbackPage />} />
    </Routes>
  );
};

export default App;
