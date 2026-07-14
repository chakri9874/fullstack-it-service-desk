import { useState } from "react";
import { BrowserRouter, Routes, Route, Navigate } from "react-router-dom";
import Navbar from "./components/Navbar";
import LoginPage from "./pages/LoginPage";
import DashboardPage from "./pages/DashboardPage";
import TicketsPage from "./pages/TicketsPage";
import UsersPage from "./pages/UsersPage";
import AssetsPage from "./pages/AssetsPage";
import EmployeePortalPage from "./pages/EmployeePortalPage";
import ITSupportPortalPage from "./pages/ITSupportPortalPage";
import AdminPortalPage from "./pages/AdminPortalPage";

function App() {
    const [loggedInUser, setLoggedInUser] = useState(() => {
        const savedUser = localStorage.getItem("loggedInUser");

        if (!savedUser) {
            return null;
        }

        try {
            return JSON.parse(savedUser);
        } catch {
            localStorage.removeItem("loggedInUser");
            localStorage.removeItem("authToken");
            return null;
        }
    });

    const handleLogin = (user) => {
        setLoggedInUser(user);
    };

    const handleLogout = () => {
        setLoggedInUser(null);
        localStorage.removeItem("loggedInUser");
        localStorage.removeItem("authToken");
    };

    const getHomeRoute = () => {
        if (!loggedInUser) {
            return "/";
        }

        if (loggedInUser.role === "EMPLOYEE") {
            return "/employee";
        }

        if (loggedInUser.role === "IT_SUPPORT") {
            return "/it-support";
        }

        if (loggedInUser.role === "ADMIN") {
            return "/admin";
        }

        return "/";
    };

    return (
        <BrowserRouter>
            <Navbar loggedInUser={loggedInUser} onLogout={handleLogout} />

            <main className="main-content">
                <Routes>
                    <Route
                        path="/"
                        element={
                            loggedInUser ? (
                                <Navigate to={getHomeRoute()} replace />
                            ) : (
                                <LoginPage onLogin={handleLogin} />
                            )
                        }
                    />

                    <Route
                        path="/employee"
                        element={
                            loggedInUser?.role === "EMPLOYEE" ? (
                                <EmployeePortalPage loggedInUser={loggedInUser} />
                            ) : (
                                <Navigate to={getHomeRoute()} replace />
                            )
                        }
                    />

                    <Route
                        path="/it-support"
                        element={
                            loggedInUser?.role === "IT_SUPPORT" ? (
                                <ITSupportPortalPage loggedInUser={loggedInUser} />
                            ) : (
                                <Navigate to={getHomeRoute()} replace />
                            )
                        }
                    />

                    <Route
                        path="/admin"
                        element={
                            loggedInUser?.role === "ADMIN" ? (
                                <AdminPortalPage loggedInUser={loggedInUser} />
                            ) : (
                                <Navigate to={getHomeRoute()} replace />
                            )
                        }
                    />

                    <Route
                        path="/dashboard"
                        element={
                            loggedInUser?.role === "ADMIN" ? (
                                <DashboardPage />
                            ) : (
                                <Navigate to={getHomeRoute()} replace />
                            )
                        }
                    />

                    <Route
                        path="/tickets"
                        element={
                            loggedInUser?.role === "ADMIN" ? (
                                <TicketsPage />
                            ) : (
                                <Navigate to={getHomeRoute()} replace />
                            )
                        }
                    />

                    <Route
                        path="/users"
                        element={
                            loggedInUser?.role === "ADMIN" ? (
                                <UsersPage />
                            ) : (
                                <Navigate to={getHomeRoute()} replace />
                            )
                        }
                    />

                    <Route
                        path="/assets"
                        element={
                            loggedInUser?.role === "ADMIN" ? (
                                <AssetsPage />
                            ) : (
                                <Navigate to={getHomeRoute()} replace />
                            )
                        }
                    />

                    <Route path="*" element={<Navigate to={getHomeRoute()} replace />} />
                </Routes>
            </main>
        </BrowserRouter>
    );
}

export default App;