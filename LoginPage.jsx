import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

// This is the login page, goes through the authentication with
// the backend, but this triggers it.
export default function LoginPage() {
  const navigate = useNavigate();
  const { refresh } = useAuth();
  const [username, setU] = useState("");
  const [password, setP] = useState("");
  const [submitting, setSubmitting] = useState(false);

  const submit = async (e) => {
    e.preventDefault();
    setSubmitting(true);
    try {
      // 1) login
      const res = await fetch("/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({ username: username.trim(), password }),
      });

      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        throw new Error(txt || "Invalid credentials");
      }

      // 2) primes auth and refresh
      await fetch("/api/me", { credentials: "include" }).catch(() => {});
      await refresh();

      // 3) takes you home with a Flashbanner telling you of the success
      navigate("/", { replace: true, state: { flash: "Logged in successfully" } });
    } catch (err) {
      alert(err.message || "Login failed");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    // formatting
    <form onSubmit={submit} className="p-6 max-w-sm mx-auto space-y-3">
      <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 12 }}>Log in</h1>
      <input
        className="border p-2 w-full"
        placeholder="username"
        value={username}
        onChange={(e) => setU(e.target.value)}
        required
      />
      <input
        className="border p-2 w-full"
        type="password"
        placeholder="password"
        value={password}
        onChange={(e) => setP(e.target.value)}
        required
      />
      <button
        className="bg-blue-600 text-white px-4 py-2 rounded disabled:opacity-50"
        disabled={submitting}
      >
        {submitting ? "Signing in..." : "Log in"}
      </button>

      <p className="text-sm text-gray-600 mt-3">
        Don’t have an account?{" "}
        <Link to="/register" className="text-blue-600 hover:underline">
          Create one
        </Link>
      </p>
    </form>
  );
}
