import { Route, Routes } from "react-router-dom";
import { HomePage } from "@/pages/HomePage.tsx";
import PATH from "@/constants/path.ts";

const App = () => {
  return (
    <Routes>
      <Route path={PATH.HOME} element={<HomePage />} />
    </Routes>
  );
};

export default App;
