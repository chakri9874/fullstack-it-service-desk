import { useState } from "react";
import api from "../api/api";

function LoginPage({ onLogin }) {
    const [formData, setFormData] = useState({
        email: "",
        password: "",
    });

    const [loading, setLoading] = useState(false);
    const [error, setError] = useState("");

    const handleInputChange = (event) => {
        const { name, value } = event.target;

        setFormData((previousData) => ({
            ...previousData,
            [name]: value,
        }));
    };

    const handleSubmit = async (event) => {
        event.preventDefault();

        try {
            setLoading(true);
            setError("");

            const response = await api.post("/auth/login", formData);

            const token = response.data.token;
            const user = response.data.user;

            localStorage.setItem("authToken", token);
            localStorage.setItem("loggedInUser", JSON.stringify(user));

            onLogin(user);
        } catch (err) {
            console.error("Login error:", err);
            setError("Invalid email or password. Please try again.");
        } finally {
            setLoading(false);
        }
    };

    return (
        <div className="auth-page">
            <section className="auth-hero">
                <p className="eyebrow">IT Service Desk</p>
                <h1>Sign in to your workspace</h1>
                <p>
                    Use your account credentials to access the correct portal. Your role is
                    verified by the backend after login.
                </p>
            </section>

            <section className="auth-card">
                <div className="auth-card-header">
                    <h2>Login</h2>
                    <p>Enter your service desk account credentials.</p>
                </div>

                {error && <div className="error-message">{error}</div>}

                <form className="auth-form" onSubmit={handleSubmit}>
                    <div className="form-group">
                        <label>Email</label>
                        <input
                            type="email"
                            name="email"
                            placeholder="Enter email address"
                            value={formData.email}
                            onChange={handleInputChange}
                            required
                        />
                    </div>

                    <div className="form-group">
                        <label>Password</label>
                        <input
                            type="password"
                            name="password"
                            placeholder="Enter password"
                            value={formData.password}
                            onChange={handleInputChange}
                            required
                        />
                    </div>

                    <button className="primary-button" type="submit" disabled={loading}>
                        {loading ? "Signing in..." : "Sign In"}
                    </button>
                </form>
            </section>
        </div>
    );
}

export default LoginPage;