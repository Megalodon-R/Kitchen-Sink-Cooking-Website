import { useEffect, useMemo, useState } from "react";

// This is the page wherein admins can delete recipes.
// could later be expanded on.
export default function AdminManageRecipesPage() {
  const [recipes, setRecipes] = useState([]);
  const [q, setQ] = useState("");
  const [loading, setLoading] = useState(true);
  const [busyId, setBusyId] = useState(null);

  // ensures a good refresh for recipes to load in
  useEffect(() => {
    let alive = true;
    (async () => {
      setLoading(true);
      try {
        const res = await fetch("/api/recipes", { credentials: "include" });
        const data = await res.json();
        if (alive) setRecipes(Array.isArray(data) ? data : []);
      } catch (e) {
        console.error(e);
        alert("Failed to load recipes");
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, []);

  // this is for tag filters, makes sure they're all standardized.
  const filtered = useMemo(() => {
    const s = q.trim().toLowerCase();
    if (!s) return recipes;
    return recipes.filter(r =>
      (r.title || "").toLowerCase().includes(s) ||
      (r.createdByName || "").toLowerCase().includes(s)
    );
  }, [recipes, q]);

  // this is for confirmation of ID that you have authority and msgs for otherwise
  const remove = async (id) => {
    if (!confirm("Delete this recipe? This cannot be undone.")) return;
    setBusyId(id);
    try {
      const res = await fetch(`/api/admin/recipes/${id}`, {
        method: "DELETE",
        credentials: "include"
      });
      if (!res.ok) {
        const txt = await res.text().catch(() => "");
        throw new Error(txt || `Delete failed (${res.status})`);
      }
      // remove from local list
      setRecipes(prev => prev.filter(r => r.recipeId !== id));
    } catch (e) {
      console.error(e);
      alert(e.message || "Delete failed");
    } finally {
      setBusyId(null);
    }
  };

  return (
    // formatting
    <div className="max-w-5xl mx-auto p-6 space-y-4">
      <div className="flex items-center justify-between gap-3">
        <h1 className="text-2xl font-semibold">Manage Recipes</h1>
        <input
          className="border rounded px-3 py-2 w-64"
          placeholder="Search by title/author"
          value={q}
          onChange={e => setQ(e.target.value)}
        />
      </div>

      {loading ? (
        <div>Loading…</div>
      ) : (
        // formatting
        <div className="grid gap-3 md:grid-cols-2">
          {filtered.map(r => (
            <div key={r.recipeId} className="border rounded p-4 bg-white flex flex-col gap-2">
              <div className="font-medium">{r.title}</div>
              <div className="text-sm text-gray-600">
                {r.makeTime != null ? `~${r.makeTime} min · ` : ""}{r.createdByName ? `by ${r.createdByName}` : ""}
              </div>
              <div className="mt-2 flex gap-2">
                <a className="border px-3 py-1 rounded" href={`/recipes/${r.recipeId}`}>Open</a>
                <button
                  className="bg-red-600 text-white px-3 py-1 rounded"
                  disabled={busyId === r.recipeId}
                  onClick={() => remove(r.recipeId)}
                >
                  {busyId === r.recipeId ? "Deleting…" : "Delete"}
                </button>
              </div>
            </div>
          ))}
          {filtered.length === 0 && (
            <div className="text-gray-600">No recipes match your search.</div>
          )}
        </div>
      )}
    </div>
  );
}
