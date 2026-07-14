import { useEffect, useState } from "react";
import { Link } from "react-router-dom";
import api from "../api/api";

function AdminPortalPage({ loggedInUser }) {
    const [activeTab, setActiveTab] = useState("ESCALATIONS");
    const [escalatedTickets, setEscalatedTickets] = useState([]);
    const [employees, setEmployees] = useState([]);
    const [supportUsers, setSupportUsers] = useState([]);
    const [selectedAssignments, setSelectedAssignments] = useState({});
    const [loading, setLoading] = useState(false);
    const [actionMessage, setActionMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const fetchAdminData = async () => {
        try {
            setLoading(true);
            setErrorMessage("");

            const escalationResponse = await api.get("/tickets/escalation-queue");
            const usersResponse = await api.get("/users");

            const allUsers = usersResponse.data || [];

            setEscalatedTickets(escalationResponse.data || []);

            setEmployees(
                allUsers.filter(
                    (user) => user.role === "EMPLOYEE"
                )
            );

            setSupportUsers(
                allUsers.filter(
                    (user) => user.role === "IT_SUPPORT"
                )
            );
        } catch (error) {
            console.error("Failed to load admin portal data:", error);
            setErrorMessage("Unable to load admin portal data right now.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchAdminData();
    }, []);

    const handleAssignmentChange = (ticketId, supportUserId) => {
        setSelectedAssignments((previousAssignments) => ({
            ...previousAssignments,
            [ticketId]: supportUserId,
        }));
    };

    const assignEscalatedTicket = async (ticketId) => {
        const selectedSupportUserId = selectedAssignments[ticketId];

        if (!selectedSupportUserId) {
            setErrorMessage("Please select an IT support user before assigning.");
            return;
        }

        try {
            setActionMessage("");
            setErrorMessage("");

            await api.patch(`/tickets/${ticketId}/assign`, {
                assignedToUserId: Number(selectedSupportUserId),
            });

            setActionMessage("Escalated ticket assigned successfully.");

            fetchAdminData();
        } catch (error) {
            console.error("Failed to assign escalated ticket:", error);
            setErrorMessage(
                error?.response?.data?.message || "Unable to assign this ticket."
            );
        }
    };

    const formatLabel = (value) => {
        if (!value) {
            return "-";
        }

        return value.toString().toLowerCase().replaceAll("_", " ");
    };

    const formatDateTime = (dateTime) => {
        if (!dateTime) {
            return "-";
        }

        return new Date(dateTime).toLocaleString();
    };

    const getUserStatusLabel = (active) => {
        return active ? "Active" : "Inactive";
    };

    const getUserStatusBadgeClass = (active) => {
        return active ? "badge badge-green" : "badge badge-gray";
    };

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <p className="eyebrow">Admin Portal</p>
                    <h1>Welcome, {loggedInUser?.fullName}</h1>
                    <p>
                        Manage escalated tickets, employees, and IT support users from one
                        admin workspace.
                    </p>
                </div>
            </div>

            {actionMessage && (
                <div className="state-message success-message">
                    {actionMessage}
                </div>
            )}

            {errorMessage && <div className="error-message">{errorMessage}</div>}

            <div className="portal-grid">
                <div className="portal-card">
                    <h2>Dashboard</h2>
                    <p>View service desk summary metrics and ticket chart data.</p>
                    <Link className="portal-link" to="/dashboard">
                        Open Dashboard
                    </Link>
                </div>

                <div className="portal-card">
                    <h2>Ticket Management</h2>
                    <p>View all tickets and support progress across the system.</p>
                    <Link className="portal-link" to="/tickets">
                        Open Tickets
                    </Link>
                </div>

                <div className="portal-card">
                    <h2>Employees</h2>
                    <p>Employee accounts loaded from the database.</p>
                    <span>{employees.length} employees</span>
                </div>

                <div className="portal-card">
                    <h2>IT Support</h2>
                    <p>Support accounts available for ticket assignment.</p>
                    <span>{supportUsers.length} support users</span>
                </div>
            </div>

            <section className="table-card">
                <div className="admin-tab-bar">
                    <button
                        className={
                            activeTab === "ESCALATIONS"
                                ? "admin-tab active-admin-tab"
                                : "admin-tab"
                        }
                        type="button"
                        onClick={() => setActiveTab("ESCALATIONS")}
                    >
                        Escalations
                    </button>

                    <button
                        className={
                            activeTab === "EMPLOYEES"
                                ? "admin-tab active-admin-tab"
                                : "admin-tab"
                        }
                        type="button"
                        onClick={() => setActiveTab("EMPLOYEES")}
                    >
                        Employees
                    </button>

                    <button
                        className={
                            activeTab === "IT_SUPPORT"
                                ? "admin-tab active-admin-tab"
                                : "admin-tab"
                        }
                        type="button"
                        onClick={() => setActiveTab("IT_SUPPORT")}
                    >
                        IT Support
                    </button>
                </div>

                {loading ? (
                    <div className="state-message">Loading admin data...</div>
                ) : (
                    <>
                        {activeTab === "ESCALATIONS" && (
                            <>
                                <div className="table-header">
                                    <div>
                                        <h2>Escalated Tickets</h2>
                                        <p>
                                            Tickets requiring admin review and manual assignment.
                                        </p>
                                    </div>

                                    <div className="record-count">
                                        {escalatedTickets.length} tickets
                                    </div>
                                </div>

                                <table className="data-table">
                                    <thead>
                                    <tr>
                                        <th>Ticket Number</th>
                                        <th>Title</th>
                                        <th>Requester</th>
                                        <th>Category</th>
                                        <th>Escalated At</th>
                                        <th>Reason</th>
                                        <th>Assign To</th>
                                        <th>Action</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    {escalatedTickets.length === 0 ? (
                                        <tr>
                                            <td className="empty-row" colSpan="8">
                                                No escalated tickets right now.
                                            </td>
                                        </tr>
                                    ) : (
                                        escalatedTickets.map((ticket) => (
                                            <tr key={ticket.id}>
                                                <td>{ticket.ticketNumber}</td>
                                                <td className="strong-cell">{ticket.title}</td>
                                                <td>{ticket.createdByName || "-"}</td>
                                                <td>{formatLabel(ticket.category)}</td>
                                                <td>{formatDateTime(ticket.escalatedAt)}</td>
                                                <td>{ticket.escalationReason || "-"}</td>
                                                <td>
                                                    <select
                                                        className="table-select"
                                                        value={selectedAssignments[ticket.id] || ""}
                                                        onChange={(event) =>
                                                            handleAssignmentChange(
                                                                ticket.id,
                                                                event.target.value
                                                            )
                                                        }
                                                    >
                                                        <option value="">Select support</option>
                                                        {supportUsers
                                                            .filter((supportUser) => supportUser.active)
                                                            .map((supportUser) => (
                                                                <option
                                                                    key={supportUser.id}
                                                                    value={supportUser.id}
                                                                >
                                                                    {supportUser.fullName}
                                                                </option>
                                                            ))}
                                                    </select>
                                                </td>
                                                <td>
                                                    <button
                                                        className="primary-button compact-button"
                                                        type="button"
                                                        onClick={() => assignEscalatedTicket(ticket.id)}
                                                    >
                                                        Assign
                                                    </button>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                    </tbody>
                                </table>
                            </>
                        )}

                        {activeTab === "EMPLOYEES" && (
                            <>
                                <div className="table-header">
                                    <div>
                                        <h2>Employees</h2>
                                        <p>Employee users stored in the database.</p>
                                    </div>

                                    <div className="record-count">
                                        {employees.length} employees
                                    </div>
                                </div>

                                <table className="data-table">
                                    <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Department</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    {employees.length === 0 ? (
                                        <tr>
                                            <td className="empty-row" colSpan="5">
                                                No employee users found.
                                            </td>
                                        </tr>
                                    ) : (
                                        employees.map((employee) => (
                                            <tr key={employee.id}>
                                                <td className="strong-cell">
                                                    {employee.fullName}
                                                </td>
                                                <td>{employee.email}</td>
                                                <td>{employee.department || "-"}</td>
                                                <td>{formatLabel(employee.role)}</td>
                                                <td>
                            <span
                                className={getUserStatusBadgeClass(
                                    employee.active
                                )}
                            >
                              {getUserStatusLabel(employee.active)}
                            </span>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                    </tbody>
                                </table>
                            </>
                        )}

                        {activeTab === "IT_SUPPORT" && (
                            <>
                                <div className="table-header">
                                    <div>
                                        <h2>IT Support Users</h2>
                                        <p>
                                            Support users who can receive and work on assigned
                                            tickets.
                                        </p>
                                    </div>

                                    <div className="record-count">
                                        {supportUsers.length} support users
                                    </div>
                                </div>

                                <table className="data-table">
                                    <thead>
                                    <tr>
                                        <th>Name</th>
                                        <th>Email</th>
                                        <th>Department</th>
                                        <th>Role</th>
                                        <th>Status</th>
                                    </tr>
                                    </thead>

                                    <tbody>
                                    {supportUsers.length === 0 ? (
                                        <tr>
                                            <td className="empty-row" colSpan="5">
                                                No IT support users found.
                                            </td>
                                        </tr>
                                    ) : (
                                        supportUsers.map((supportUser) => (
                                            <tr key={supportUser.id}>
                                                <td className="strong-cell">
                                                    {supportUser.fullName}
                                                </td>
                                                <td>{supportUser.email}</td>
                                                <td>{supportUser.department || "-"}</td>
                                                <td>{formatLabel(supportUser.role)}</td>
                                                <td>
                            <span
                                className={getUserStatusBadgeClass(
                                    supportUser.active
                                )}
                            >
                              {getUserStatusLabel(supportUser.active)}
                            </span>
                                                </td>
                                            </tr>
                                        ))
                                    )}
                                    </tbody>
                                </table>
                            </>
                        )}
                    </>
                )}
            </section>
        </div>
    );
}

export default AdminPortalPage;