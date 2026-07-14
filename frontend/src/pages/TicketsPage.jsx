import { useEffect, useState } from "react";
import api from "../api/api";

function TicketsPage() {
    const [tickets, setTickets] = useState([]);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchTickets();
    }, []);

    const fetchTickets = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await api.get("/tickets");
            setTickets(response.data);
        } catch (err) {
            console.error("Tickets API error:", err);
            setError("Unable to load tickets. Please check if the backend is running.");
        } finally {
            setLoading(false);
        }
    };

    const formatValue = (value) => {
        if (!value) {
            return "not assigned";
        }

        return value.toString().toLowerCase().replaceAll("_", " ");
    };

    const formatDateTime = (dateTime) => {
        if (!dateTime) {
            return "-";
        }

        return new Date(dateTime).toLocaleString();
    };

    const getProgressLabel = (ticket) => {
        if (ticket.status === "RESOLVED") {
            return "Resolved";
        }

        if (ticket.assignmentStatus === "PENDING_ACCEPTANCE") {
            return "Pending Support Review";
        }

        if (ticket.assignmentStatus === "ACCEPTED") {
            if (ticket.status === "ON_HOLD") {
                return "On Hold";
            }

            return "In Support Review";
        }

        if (ticket.assignmentStatus === "ESCALATED") {
            return "Escalated to Admin";
        }

        return "Waiting for Assignment";
    };

    const getStatusBadgeClass = (status) => {
        if (!status) {
            return "badge badge-gray";
        }

        if (status === "OPEN") {
            return "badge badge-blue";
        }

        if (status === "IN_PROGRESS") {
            return "badge badge-orange";
        }

        if (status === "RESOLVED") {
            return "badge badge-green";
        }

        if (status === "ON_HOLD") {
            return "badge badge-purple";
        }

        if (status === "CLOSED") {
            return "badge badge-gray";
        }

        if (status === "CANCELLED") {
            return "badge badge-red";
        }

        return "badge badge-gray";
    };

    if (loading) {
        return (
            <div className="page">
                <h1>Tickets</h1>
                <p>Loading tickets...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="page">
                <h1>Tickets</h1>
                <p>{error}</p>
            </div>
        );
    }

    return (
        <div className="page">
            <div className="page-header">
                <div>
                    <p className="eyebrow">Service Desk</p>
                    <h1>Tickets</h1>
                    <p>View support tickets and assignment progress.</p>
                </div>
            </div>

            <div className="table-card">
                <div className="table-header">
                    <div>
                        <h2>Ticket List</h2>
                        <p>Live ticket records from the Spring Boot backend.</p>
                    </div>

                    <div className="record-count">{tickets.length} tickets</div>
                </div>

                <table className="data-table">
                    <thead>
                    <tr>
                        <th>Ticket Number</th>
                        <th>Title</th>
                        <th>Category</th>
                        <th>Progress</th>
                        <th>Created By</th>
                        <th>Assigned To</th>
                        <th>Created At</th>
                        <th>Updated At</th>
                    </tr>
                    </thead>

                    <tbody>
                    {tickets.length === 0 ? (
                        <tr>
                            <td colSpan="8" className="empty-row">
                                No tickets found.
                            </td>
                        </tr>
                    ) : (
                        tickets.map((ticket) => (
                            <tr key={ticket.id}>
                                <td>{ticket.ticketNumber}</td>
                                <td className="strong-cell">{ticket.title}</td>
                                <td>{formatValue(ticket.category)}</td>
                                <td>
                    <span className={getStatusBadgeClass(ticket.status)}>
                      {getProgressLabel(ticket)}
                    </span>
                                </td>
                                <td>{ticket.createdByName || "Not available"}</td>
                                <td>{ticket.assignedToName || "Not assigned"}</td>
                                <td>{formatDateTime(ticket.createdAt)}</td>
                                <td>{formatDateTime(ticket.updatedAt)}</td>
                            </tr>
                        ))
                    )}
                    </tbody>
                </table>
            </div>
        </div>
    );
}

export default TicketsPage;
