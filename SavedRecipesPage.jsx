import { useEffect, useState } from "react";

// This is the page where saved recipes are kept.
// the idea here was similar to the tiktok "liked" page.

export default function SavedRecipesPage() {
  const [me, setMe] = useState(null);
  const [saved, setSaved] = useState([]);
  const [loading, setLoading] = useState(true);

  // basically returns the SavedRecipePage of a specific individual
  // but only if they're logged in.
  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        const [meRes, savedRes] = await Promise.all([
          fetch("/api/me", { credentials: "include" }),
          fetch("/api/me/saved", { credentials: "include" }),
        ]);
        if (meRes.status === 401) { setMe(null); setSaved([]); return; }
        const meJson = await meRes.json();
        const savedJson = await savedRes.json();
        if (!alive) return;
        setMe(meJson);
        setSaved(Array.isArray(savedJson) ? savedJson : []);
      } catch (e) {
        console.error("Saved page load failed", e);
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, []);

  if (loading) return <div className="p-6">Loading…</div>;
  if (!me) return <div className="p-6">Please log in to view saved recipes.</div>;

  return (
    // formatting
    <div className="max-w-4xl mx-auto p-6 space-y-6">
      {/* Profile header */}
      <div className="border rounded p-4 bg-white">
        <div className="text-xl font-semibold">{me.username}</div>
        <div className="text-gray-600 whitespace-pre-wrap">{me.bio || "No bio yet."}</div>
      </div>

      {/* Saved list */}
      <div className="space-y-3">
        <h2 className="text-lg font-semibold">Saved recipes</h2>
        {saved.length === 0 ? (
          <div className="text-gray-600">No saved recipes yet.</div>
        ) : (
          <ul className="grid gap-3 md:grid-cols-2">
            {saved.map(r => (
              <li key={r.recipeId} className="border rounded p-4 bg-white">
                <a className="text-blue-600 hover:underline" href={`/recipes/${r.recipeId}`}>
                  {r.title}
                </a>
                {r.makeTime != null && (
                  <div className="text-sm text-gray-600">~{r.makeTime} min</div>
                )}
                {r.createdByName && (
                  <div className="text-sm text-gray-600">by {r.createdByName}</div>
                )}
              </li>
            ))}
          </ul>
        )}
      </div>
    </div>
  );
}
