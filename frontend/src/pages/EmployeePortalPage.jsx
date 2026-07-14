import { useEffect, useState } from "react";
import api from "../api/api";

function EmployeePortalPage({ loggedInUser }) {
  const [tickets, setTickets] = useState([]);
  const [loadingTickets, setLoadingTickets] = useState(false);
  const [submitting, setSubmitting] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const [formData, setFormData] = useState({
    title: "",
    description: "",
    category: "SOFTWARE",
  });

  const fetchMyTickets = async () => {
    if (!loggedInUser?.id) {
      return;
    }

    try {
      setLoadingTickets(true);
      setErrorMessage("");

      const response = await api.get(`/tickets/created-by/${loggedInUser.id}`);

      setTickets(response.data);
    } catch (error) {
      console.error("Failed to fetch employee tickets:", error);
      setErrorMessage("Unable to load your tickets right now.");
    } finally {
      setLoadingTickets(false);
    }
  };

  useEffect(() => {
    fetchMyTickets();
  }, [loggedInUser?.id]);

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
      setSubmitting(true);
      setSuccessMessage("");
      setErrorMessage("");

      const requestBody = {
        title: formData.title,
        description: formData.description,
        category: formData.category,
        createdByUserId: loggedInUser.id,
      };

      await api.post("/tickets", requestBody);

      setSuccessMessage("Ticket created successfully.");

      setFormData({
        title: "",
        description: "",
        category: "SOFTWARE",
      });

      fetchMyTickets();
    } catch (error) {
      console.error("Failed to create ticket:", error);
      setErrorMessage(
          "Unable to create ticket. Please check the form and try again."
      );
    } finally {
      setSubmitting(false);
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

  const getProgressLabel = (ticket) => {
    if (ticket.status === "RESOLVED") {
      return "Resolved";
    }

    if (ticket.assignmentStatus === "PENDING_ACCEPTANCE") {
      return "Assigned to Support";
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

  return (
      <div className="page">
        <div className="page-header">
          <div>
            <p className="eyebrow">Employee Portal</p>
            <h1>Welcome, {loggedInUser?.fullName}</h1>
            <p>
              Raise support tickets and track progress from your employee account.
            </p>
          </div>
        </div>

        {successMessage && (
            <div className="state-message success-message">
              {successMessage}
            </div>
        )}

        {errorMessage && <div className="error-message">{errorMessage}</div>}

        <div className="employee-layout">
          <section className="portal-card">
            <h2>Raise a Ticket</h2>
            <p>
              Submit an IT support request. The system will route it to an
              available support team member.
            </p>

            <form className="ticket-form" onSubmit={handleSubmit}>
              <div className="form-group">
                <label>Title</label>
                <input
                    type="text"
                    name="title"
                    placeholder="Example: Unable to access payroll portal"
                    value={formData.title}
                    onChange={handleInputChange}
                    maxLength="150"
                    required
                />
              </div>

              <div className="form-group">
                <label>Description</label>
                <textarea
                    name="description"
                    placeholder="Describe the issue clearly..."
                    value={formData.description}
                    onChange={handleInputChange}
                    rows="5"
                    required
                />
              </div>

              <div className="form-group">
                <label>Category</label>
                <select
                    name="category"
                    value={formData.category}
                    onChange={handleInputChange}
                    required
                >
                  <option value="HARDWARE">Hardware</option>
                  <option value="SOFTWARE">Software</option>
                  <option value="NETWORK">Network</option>
                  <option value="ACCESS">Access</option>
                  <option value="SECURITY">Security</option>
                  <option value="EMAIL">Email</option>
                  <option value="ACCOUNT">Account</option>
                  <option value="OTHER">Other</option>
                </select>
              </div>

              <button
                  className="primary-button"
                  type="submit"
                  disabled={submitting}
              >
                {submitting ? "Creating Ticket..." : "Create Ticket"}
              </button>
            </form>
          </section>

          <section className="portal-card">
            <h2>Account</h2>
            <p>
              Email: <strong>{loggedInUser?.email}</strong>
            </p>
            <p>
              Role: <strong>{loggedInUser?.role}</strong>
            </p>
          </section>
        </div>

        <section className="table-card">
          <div className="table-header">
            <div>
              <h2>My Tickets</h2>
              <p>Tickets created by your logged-in employee account.</p>
            </div>

            <div className="record-count">{tickets.length} tickets</div>
          </div>

          {loadingTickets ? (
              <div className="state-message">Loading your tickets...</div>
          ) : (
              <table className="data-table">
                <thead>
                <tr>
                  <th>Ticket Number</th>
                  <th>Title</th>
                  <th>Category</th>
                  <th>Progress</th>
                  <th>Assigned To</th>
                  <th>Created At</th>
                  <th>Updated At</th>
                  <th>Resolved At</th>
                </tr>
                </thead>

                <tbody>
                {tickets.length === 0 ? (
                    <tr>
                      <td className="empty-row" colSpan="8">
                        You have not created any tickets yet.
                      </td>
                    </tr>
                ) : (
                    tickets.map((ticket) => (
                        <tr key={ticket.id}>
                          <td>{ticket.ticketNumber}</td>
                          <td className="strong-cell">{ticket.title}</td>
                          <td>{formatLabel(ticket.category)}</td>
                          <td>
                      <span className={getStatusBadgeClass(ticket.status)}>
                        {getProgressLabel(ticket)}
                      </span>
                          </td>
                          <td>{ticket.assignedToName || "Not assigned yet"}</td>
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
          )}
        </section>
      </div>
  );
}

export default EmployeePortalPage;
