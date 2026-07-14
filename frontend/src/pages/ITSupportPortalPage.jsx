import { useEffect, useState } from "react";
import api from "../api/api";

function ITSupportPortalPage({ loggedInUser }) {
    const [supportStats, setSupportStats] = useState(null);
    const [pendingTickets, setPendingTickets] = useState([]);
    const [assignedTickets, setAssignedTickets] = useState([]);
    const [loading, setLoading] = useState(false);
    const [actionMessage, setActionMessage] = useState("");
    const [errorMessage, setErrorMessage] = useState("");

    const fetchSupportData = async () => {
        if (!loggedInUser?.id) {
            return;
        }

        try {
            setLoading(true);
            setErrorMessage("");

            const statsResponse = await api.get("/tickets/support-stats");
            const queueResponse = await api.get("/tickets/support-queue");

            const assignedResponse = await api.get(
                `/tickets/assigned-to/${loggedInUser.id}`
            );

            setSupportStats(statsResponse.data);
            setPendingTickets(queueResponse.data);

            setAssignedTickets(
                assignedResponse.data.filter(
                    (ticket) => ticket.assignmentStatus === "ACCEPTED"
                )
            );
        } catch (error) {
            console.error("Failed to load IT support data:", error);
            setErrorMessage("Unable to load support tickets right now.");
        } finally {
            setLoading(false);
        }
    };

    useEffect(() => {
        fetchSupportData();
    }, [loggedInUser?.id]);

    const acceptTicket = async (ticketId) => {
        try {
            setActionMessage("");
            setErrorMessage("");

            await api.patch(`/tickets/${ticketId}/accept`);

            setActionMessage("Ticket accepted.");

            fetchSupportData();
        } catch (error) {
            console.error("Failed to accept ticket:", error);
            setErrorMessage("Unable to accept this ticket.");
        }
    };

    const rejectTicket = async (ticketId) => {
        try {
            setActionMessage("");
            setErrorMessage("");

            await api.patch(`/tickets/${ticketId}/reject`);

            setActionMessage("Ticket rejected and moved for reassignment.");

            fetchSupportData();
        } catch (error) {
            console.error("Failed to reject ticket:", error);
            setErrorMessage(
                error?.response?.data?.message || "Unable to reject this ticket."
            );
        }
    };

    const updateTicketStatus = async (ticketId, status) => {
        try {
            setActionMessage("");
            setErrorMessage("");

            await api.patch(`/tickets/${ticketId}/status`, {
                status,
            });

            setActionMessage("Ticket status updated.");

            fetchSupportData();
        } catch (error) {
            console.error("Failed to update ticket status:", error);
            setErrorMessage("Unable to update ticket status.");
        }
    };

    const formatLabel = (value) => {
        if (!value) {
            return "-";
        }

        return value.toLowerCase().replaceAll("_", " ");
    };

    const formatDateTime = (dateTime) => {
        if (!dateTime) {
            return "-";
        }

        return new Date(dateTime).toLocaleString();
    };

    const getStatusBadgeClass = (status) => {
        if (status === "OPEN") {
            return "badge badge-blue";
        }

        if (status === "IN_PROGRESS") {
            return "badge badge-orange";
        }

        if (status === "RESOLVED" || status === "CLOSED") {
            return "badge badge-green";
        }

        if (status === "ON_HOLD") {
            return "badge badge-purple";
        }

        if (status === "CANCELLED") {
            return "badge badge-gray";
        }

        return "badge badge-gray";
    };

    const getRejectionUsagePercent = () => {
        if (!supportStats?.weeklyRejectionLimit) {
            return 0;
        }

        return Math.min(
            (supportStats.rejectionsUsedThisWeek /
                supportStats.weeklyRejectionLimit) *
            100,
            100
        );
    };

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <p className="eyebrow">IT Support Portal</p>
                    <h1>Welcome, {loggedInUser?.fullName}</h1>
                    <p>
                        Review routed tickets, manage accepted work, and track weekly
                        rejection usage.
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
                    <h2>Pending Review</h2>
                    <p>Tickets routed to you and waiting for your response.</p>
                    <span>{pendingTickets.length} pending</span>
                </div>

                <div className="portal-card">
                    <h2>Active Work</h2>
                    <p>Tickets currently assigned to your support account.</p>
                    <span>{assignedTickets.length} active</span>
                </div>

                <div className="portal-card">
                    <h2>Weekly Rejections</h2>
                    <p>
                        Used {supportStats?.rejectionsUsedThisWeek || 0} of{" "}
                        {supportStats?.weeklyRejectionLimit || 2} available rejections this
                        week.
                    </p>

                    <div className="usage-bar">
                        <div
                            className="usage-bar-fill"
                            style={{ width: `${getRejectionUsagePercent()}%` }}
                        ></div>
                    </div>

                    <span>
            {supportStats?.remainingRejectionsThisWeek || 0} remaining
          </span>
                </div>
            </div>

            {loading ? (
                <section className="table-card">
                    <div className="state-message">Loading support tickets...</div>
                </section>
            ) : (
                <>
                    <section className="table-card">
                        <div className="table-header">
                            <div>
                                <h2>Pending Review</h2>
                                <p>Accept or reject tickets routed to your support account.</p>
                            </div>

                            <div className="record-count">
                                {pendingTickets.length} tickets
                            </div>
                        </div>

                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>Ticket Number</th>
                                <th>Title</th>
                                <th>Requester</th>
                                <th>Category</th>
                                <th>Status</th>
                                <th>Created At</th>
                                <th>Updated At</th>
                                <th>Action</th>
                            </tr>
                            </thead>

                            <tbody>
                            {pendingTickets.length === 0 ? (
                                <tr>
                                    <td className="empty-row" colSpan="8">
                                        No tickets are waiting for your review.
                                    </td>
                                </tr>
                            ) : (
                                pendingTickets.map((ticket) => (
                                    <tr key={ticket.id}>
                                        <td>{ticket.ticketNumber}</td>
                                        <td className="strong-cell">{ticket.title}</td>
                                        <td>{ticket.createdByName || "-"}</td>
                                        <td>{formatLabel(ticket.category)}</td>
                                        <td>
                        <span className={getStatusBadgeClass(ticket.status)}>
                          Awaiting Review
                        </span>
                                        </td>
                                        <td>{formatDateTime(ticket.createdAt)}</td>
                                        <td>{formatDateTime(ticket.updatedAt)}</td>
                                        <td>
                                            <div className="button-row">
                                                <button
                                                    className="primary-button compact-button"
                                                    type="button"
                                                    onClick={() => acceptTicket(ticket.id)}
                                                >
                                                    Accept
                                                </button>

                                                <button
                                                    className="danger-button compact-button"
                                                    type="button"
                                                    onClick={() => rejectTicket(ticket.id)}
                                                >
                                                    Reject
                                                </button>
                                            </div>
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </section>

                    <section className="table-card">
                        <div className="table-header">
                            <div>
                                <h2>Active Work</h2>
                                <p>Tickets assigned to your IT support account.</p>
                            </div>

                            <div className="record-count">
                                {assignedTickets.length} tickets
                            </div>
                        </div>

                        <table className="data-table">
                            <thead>
                            <tr>
                                <th>Ticket Number</th>
                                <th>Title</th>
                                <th>Requester</th>
                                <th>Category</th>
                                <th>Status</th>
                                <th>Created At</th>
                                <th>Updated At</th>
                                <th>Resolved At</th>
                            </tr>
                            </thead>

                            <tbody>
                            {assignedTickets.length === 0 ? (
                                <tr>
                                    <td className="empty-row" colSpan="8">
                                        No active work items yet.
                                    </td>
                                </tr>
                            ) : (
                                assignedTickets.map((ticket) => (
                                    <tr key={ticket.id}>
                                        <td>{ticket.ticketNumber}</td>
                                        <td className="strong-cell">{ticket.title}</td>
                                        <td>{ticket.createdByName || "-"}</td>
                                        <td>{formatLabel(ticket.category)}</td>
                                        <td>
                                            <select
                                                className="table-select"
                                                value={ticket.status}
                                                onChange={(event) =>
                                                    updateTicketStatus(
                                                        ticket.id,
                                                        event.target.value
                                                    )
                                                }
                                            >
                                                <option value="IN_PROGRESS">In Support Review</option>
                                                <option value="ON_HOLD">On Hold</option>
                                                <option value="RESOLVED">Resolved</option>
                                            </select>
                                        </td>
                                        <td>{formatDateTime(ticket.createdAt)}</td>
                                        <td>{formatDateTime(ticket.updatedAt)}</td>
                                        <td>
                                            {ticket.resolvedAt
                                                ? formatDateTime(ticket.resolvedAt)
                                                : "Not resolved yet"}
                                        </td>
                                    </tr>
                                ))
                            )}
                            </tbody>
                        </table>
                    </section>
                </>
            )}
        </div>
    );
}

export default ITSupportPortalPage;
