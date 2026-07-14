import { useEffect, useMemo, useState } from "react";
import api from "../api/api";

function UsersPage() {
    const [users, setUsers] = useState([]);
    const [selectedRole, setSelectedRole] = useState("ALL");
    const [loading, setLoading] = useState(true);
    const [errorMessage, setErrorMessage] = useState("");

    const fetchUsers = async () => {
        try {
            setLoading(true);
            setErrorMessage("");

            const response = await api.get("/users");

            setUsers(response.data || []);
        } catch (error) {
            console.error("Users API error:", error);
            setErrorMessage("Unable to load users. Please check if the backend is running.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchUsers();
    }, []);

    const filteredUsers = useMemo(() => {
        if (selectedRole === "ALL") {
            return users;
        }

        return users.filter((user) => user.role === selectedRole);
    }, [users, selectedRole]);

    const employeeCount = users.filter((user) => user.role === "EMPLOYEE").length;
    const supportCount = users.filter((user) => user.role === "IT_SUPPORT").length;
    const adminCount = users.filter((user) => user.role === "ADMIN").length;

    const formatLabel = (value) => {
        if (!value) {
            return "-";
        }

        return value.toString().toLowerCase().replaceAll("_", " ");
    };

    const getStatusBadgeClass = (active) => {
        return active ? "badge badge-green" : "badge badge-gray";
    };

    const getStatusLabel = (active) => {
        return active ? "Active" : "Inactive";
    };

    if (loading) {
        return (
            <div className="page">
                <h1>Users</h1>
                <p>Loading users...</p>
            </div>
        );
    }

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <p className="eyebrow">User Management</p>
                    <h1>Users</h1>
                    <p>
                        View employee, IT support, and admin accounts stored in the database.
                    </p>
                </div>
            </div>

            {errorMessage && <div className="error-message">{errorMessage}</div>}

            <div className="portal-grid">
                <div className="portal-card">
                    <h2>Total Users</h2>
                    <p>All user accounts available in the system.</p>
                    <span>{users.length} users</span>
                </div>

                <div className="portal-card">
                    <h2>Employees</h2>
                    <p>Employees who can create and track support tickets.</p>
                    <span>{employeeCount} employees</span>
                </div>

                <div className="portal-card">
                    <h2>IT Support</h2>
                    <p>Support users who can receive and resolve tickets.</p>
                    <span>{supportCount} support users</span>
                </div>

                <div className="portal-card">
                    <h2>Admins</h2>
                    <p>Admin users who manage escalations and system data.</p>
                    <span>{adminCount} admins</span>
                </div>
            </div>

            <section className="table-card">
                <div className="table-header">
                    <div>
                        <h2>User Directory</h2>
                        <p>Filter users by role and view account details.</p>
                    </div>

                    <div className="record-count">
                        {filteredUsers.length} shown
                    </div>
                </div>

                <div className="filter-row">
                    <div className="form-group filter-control">
                        <label>User Type</label>
                        <select
                            value={selectedRole}
                            onChange={(event) => setSelectedRole(event.target.value)}
                        >
                            <option value="ALL">All Users</option>
                            <option value="EMPLOYEE">Employees</option>
                            <option value="IT_SUPPORT">IT Support</option>
                            <option value="ADMIN">Admins</option>
                        </select>
                    </div>
                </div>

                <table className="data-table">
                    <thead>
                    <tr>
                        <th>Name</th>
                        <th>Email</th>
                        <th>Role</th>
                        <th>Department</th>
                        <th>Status</th>
                    </tr>
                    </thead>

                    <tbody>
                    {filteredUsers.length === 0 ? (
                        <tr>
                            <td className="empty-row" colSpan="5">
                                No users found for the selected user type.
                            </td>
                        </tr>
                    ) : (
                        filteredUsers.map((user) => (
                            <tr key={user.id}>
                                <td className="strong-cell">{user.fullName}</td>
                                <td>{user.email}</td>
                                <td>{formatLabel(user.role)}</td>
                                <td>{user.department || "-"}</td>
                                <td>
                    <span className={getStatusBadgeClass(user.active)}>
                      {getStatusLabel(user.active)}
                    </span>
                                </td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </section>
        </div>
    );
}

export default UsersPage;