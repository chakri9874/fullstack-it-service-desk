import { NavLink } from "react-router-dom";

function Navbar({ loggedInUser, onLogout }) {
    if (!loggedInUser) {
        return (
            <nav className="navbar">
                <div className="navbar-title">
                    <span className="brand-mark">IT</span>
                    <span>Service Desk</span>
                </div>
            </nav>
        );
    }

    const isAdmin = loggedInUser.role === "ADMIN";

    return (
        <nav className="navbar">
            <div className="navbar-title">
                <span className="brand-mark">IT</span>
                <span>Service Desk</span>
            </div>

            <div className="navbar-links">
                {isAdmin && (
                    <>
                        <NavLink
                            to="/admin"
                            className={({ isActive }) =>
                                isActive ? "navbar-link active-navbar-link" : "navbar-link"
                            }
                        >
                            Admin Portal
                        </NavLink>

                        <NavLink
                            to="/dashboard"
                            className={({ isActive }) =>
                                isActive ? "navbar-link active-navbar-link" : "navbar-link"
                            }
                        >
                            Dashboard
                        </NavLink>

                        <NavLink
                            to="/tickets"
                            className={({ isActive }) =>
                                isActive ? "navbar-link active-navbar-link" : "navbar-link"
                            }
                        >
                            Tickets
                        </NavLink>

                        <NavLink
                            to="/users"
                            className={({ isActive }) =>
                                isActive ? "navbar-link active-navbar-link" : "navbar-link"
                            }
                        >
                            Users
                        </NavLink>
                    </>
                )}

                <div className="user-pill">{loggedInUser.fullName}</div>

                <button className="logout-button" onClick={onLogout}>
                    Logout
                </button>
            </div>
        </nav>
    );
}

export default Navbar;