import { useState } from "react";
import { Link, useNavigate } from "react-router-dom";

// The registration page, allows a user to create an account.

export default function RegisterPage() {
  const navigate = useNavigate();
  const [form, setForm] = useState({
    username: "",
    password: "",
    confirm: "",
    secPin: "",
    passcode: "",
    bio: "",
  });
  const [submitting, setSubmitting] = useState(false);

  const onChange = (e) => setForm({ ...form, [e.target.name]: e.target.value });

  const submit = async (e) => {
    e.preventDefault();

    // verifies passwors match
    if (form.password !== form.confirm) {
      alert("Passwords do not match");
      return;
    }

    // check for non-digit characters
    if (!/^\d+$/.test(String(form.secPin))) {
      alert("Security PIN must be numbers only");
      return;
    }

    setSubmitting(true);
    try {
      // 1) registers user
      const regBody = {
        username: form.username.trim(),
        password: form.password,
        secPin: Number(form.secPin),
        passcode: form.passcode.trim(),
        bio: form.bio || null,
      };

      console.log("register body", {
      username: form.username.trim(),
      passcode: form.passcode.trim(),
      });

      const reg = await fetch("/auth/register", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        body: JSON.stringify(regBody),
      });

      if (!reg.ok) {
        const txt = await reg.text().catch(() => "");
        console.error("Register failed:", reg.status, txt);
        if (reg.status === 409) throw new Error("409 Conflict — Username already taken");
        throw new Error(`${reg.status} ${reg.statusText} — ${txt || "Registration failed"}`);
      }

      // 2) logins in and sets httpOnly cookies
      const login = await fetch("/auth/login", {
        method: "POST",
        headers: { "Content-Type": "application/json" },
        credentials: "include",
        body: JSON.stringify({
          username: form.username.trim(),
          password: form.password,
        }),
      });
      if (!login.ok) throw new Error("Registered, but login failed");

      // Prime auth state so UI updates immediately
      await fetch("/api/me", { credentials: "include" }).catch(() => {});

      // 3) Done
      navigate("/"); // navigates to "/recipes"
    } catch (err) {
      console.error(err);
      alert(err.message || "Something went wrong");
    } finally {
      setSubmitting(false);
    }
  };

  return (
    <div style={{ padding: 24, maxWidth: 520, margin: "0 auto" }}>
      {/* formatting */}
      <style>{`
        .field { width: 100%;
          border: 1px solid rgba(0,0,0,0.18);
          border-radius: 0;
          padding: 10px 12px;
          background: #fff;
          outline: none;
          transition: border-color .2s ease, box-shadow .2s ease, background-color .2s ease;
        }
        .field:hover {
          border-color: rgba(0,0,0,0.28);
        }
        .field:focus,
        .field:focus-visible {
          border-color: #6b7280;
          box-shadow: 0 0 0 3px rgba(107,114,128,.22);
          background: #fff;
        }
        .field::placeholder { color: #9aa3af; }
        .btn {
        border: 1px solid rgba(0,0,0,.2);
        border-radius: 10px;
        padding: 10px 14px;
        background: #1f6feb; color: #fff; font-weight: 600;
        transition: filter .2s ease, transform .02s ease;
        }
        .btn:disabled { filter: grayscale(.2) opacity(.85); cursor: not-allowed; }
        .btn:hover { filter: brightness(1.05); }
        .btn:active { transform: translateY(1px); }
      `}</style>
      <h1 style={{ fontSize: 24, fontWeight: 700, marginBottom: 12 }}>Create an Account</h1>
  
      <form onSubmit={submit} style={{ display: "grid", gap: 12 }}>
        <input
          className="field"
          name="username"
          placeholder="Username"
          value={form.username}
          onChange={onChange}
          required
        />

        <input
          className="field"
          type="password"
          name="password"
          placeholder="Password"
          value={form.password}
          onChange={onChange}
          required
        />

        <input
          className="field"
          type="password"
          name="confirm"
          placeholder="Confirm password"
          value={form.confirm}
          onChange={onChange}
          required
        />

        <input
          className="field"
          type="number"
          inputMode="numeric"
          name="secPin"
          placeholder="Security PIN (numbers only)"
          value={form.secPin}
          onChange={onChange}
          required
        />

        <input
          className="field"
          name="passcode"
          placeholder="Admin/Moderator passcode (optional)"
          value={form.passcode}
          onChange={onChange}
        />

        <textarea
          className="field"
          name="bio"
          placeholder="Bio (optional)"
          value={form.bio}
          onChange={onChange}
          rows={3}
        />

        <button type="submit" disabled={submitting}>
          {submitting ? "Creating..." : "Create account"}
        </button>
      </form>

      <p style={{ marginTop: 12, fontSize: 14 }}>
        Already have an account? <Link to="/login">Log in</Link>
      </p>
    </div>
  );
}
