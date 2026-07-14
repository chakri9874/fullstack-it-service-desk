import { useEffect, useState } from "react";
import {
    BarChart,
    Bar,
    XAxis,
    YAxis,
    Tooltip,
    ResponsiveContainer,
} from "recharts";
import api from "../api/api";

function DashboardPage() {
    const [dashboardData, setDashboardData] = useState(null);
    const [loading, setLoading] = useState(true);
    const [error, setError] = useState("");

    useEffect(() => {
        fetchDashboardData();
    }, []);

    const fetchDashboardData = async () => {
        try {
            setLoading(true);
            setError("");

            const response = await api.get("/dashboard");
            setDashboardData(response.data);
        } catch (err) {
            console.error("Dashboard API error:", err);
            setError("Unable to load dashboard data. Please check if the backend is running.");
        } finally {
            setLoading(false);
        }
    };

    const formatChartLabel = (label) => {
        return label.toLowerCase().replaceAll("_", " ");
    };

    if (loading) {
        return (
            <div className="page">
                <h1>Dashboard</h1>
                <p>Loading dashboard data...</p>
            </div>
        );
    }

    if (error) {
        return (
            <div className="page">
                <h1>Dashboard</h1>
                <p>{error}</p>
            </div>
        );
    }

    const summary = dashboardData?.summary;
    const ticketsByStatus = dashboardData?.ticketsByStatus || [];
    const ticketsByCategory = dashboardData?.ticketsByCategory || [];

    return (
        <div className="page">
            <h1>Dashboard</h1>
            <p>View service desk summary metrics and ticket chart data.</p>

            <div className="card-grid">
                <div className="card">
                    <h3>Total Tickets</h3>
                    <p>{summary?.totalTickets}</p>
                </div>

                <div className="card">
                    <h3>Open Tickets</h3>
                    <p>{summary?.openTickets}</p>
                </div>

                <div className="card">
                    <h3>In Progress</h3>
                    <p>{summary?.inProgressTickets}</p>
                </div>

                <div className="card">
                    <h3>Resolved Tickets</h3>
                    <p>{summary?.resolvedTickets}</p>
                </div>

                <div className="card">
                    <h3>Escalated Tickets</h3>
                    <p>{summary?.escalatedTickets}</p>
                </div>

                <div className="card">
                    <h3>Total Users</h3>
                    <p>{summary?.totalUsers}</p>
                </div>

                <div className="card">
                    <h3>Active Users</h3>
                    <p>{summary?.activeUsers}</p>
                </div>
            </div>

            <div className="charts-grid">
                <div className="chart-card">
                    <h2>Tickets by Status</h2>

                    <ResponsiveContainer width="100%" height={330}>
                        <BarChart
                            data={ticketsByStatus}
                            margin={{ top: 20, right: 20, left: 0, bottom: 35 }}
                        >
                            <XAxis
                                dataKey="label"
                                interval={0}
                                height={50}
                                tickFormatter={formatChartLabel}
                                tick={{ fontSize: 10, fontStyle: "normal" }}
                            />
                            <YAxis
                                allowDecimals={false}
                                tick={{ fontSize: 11, fontStyle: "normal" }}
                            />
                            <Tooltip labelFormatter={formatChartLabel} />
                            <Bar dataKey="count" fill="#2563eb" />
                        </BarChart>
                    </ResponsiveContainer>
                </div>

                <div className="chart-card">
                    <h2>Tickets by Category</h2>

                    <ResponsiveContainer width="100%" height={330}>
                        <BarChart
                            data={ticketsByCategory}
                            margin={{ top: 20, right: 20, left: 0, bottom: 35 }}
                        >
                            <XAxis
                                dataKey="label"
                                interval={0}
                                height={50}
                                tickFormatter={formatChartLabel}
                                tick={{ fontSize: 10, fontStyle: "normal" }}
                            />
                            <YAxis
                                allowDecimals={false}
                                tick={{ fontSize: 11, fontStyle: "normal" }}
                            />
                            <Tooltip labelFormatter={formatChartLabel} />
                            <Bar dataKey="count" fill="#f97316" />
                        </BarChart>
                    </ResponsiveContainer>
                </div>
            </div>
        </div>
    );
}

export default DashboardPage;
