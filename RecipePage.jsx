
import { useEffect, useMemo, useState } from "react";
import { Link } from "react-router-dom";

// This is the page that displays all created recipes and some of their info.
// also sorts through tags

export default function RecipePage() {
  const [recipes, setRecipes] = useState([]);
  const [tags, setTags] = useState([]);

  // initializing URL params
  const params = new URLSearchParams(window.location.search);
  const initQ = params.get("q") || "";
  const initInclude = params.get("include")?.split(",").filter(Boolean).map(Number) || [];
  const initExclude = params.get("exclude")?.split(",").filter(Boolean).map(Number) || [];

  const [q, setQ] = useState(initQ);
  const [includeIds, setIncludeIds] = useState(initInclude);
  const [excludeIds, setExcludeIds] = useState(initExclude);

  // helper to change backend tag id field into a number
  const normalizeTag = (t) => {
    const rawId = t.tagId ?? t.id ?? t.tag_id;
    const id = Number(rawId);
    return {
      id,
      name: t.name,
    };
  };

  // load tags
  useEffect(() => {
    fetch("/api/tags")
      .then(r => {
        if (!r.ok) throw new Error(`tags ${r.status}`);
        return r.json();
      })
      .then(data => {
        const arr = Array.isArray(data) ? data.map(normalizeTag) : [];
        // filter out any tags with invalid id (NaN)
        const cleaned = arr.filter(t => Number.isFinite(t.id));
        setTags(cleaned);
      })
      .catch(err => {
        console.error("Error loading tags", err);
        setTags([]);
      });
  }, []);

  // build query string
  const qs = useMemo(() => {
    const p = new URLSearchParams();
    const qText = (q || "").trim();
    if (qText) {
      p.set("q", qText);
      // no separate author filter 
      // q already covers author
    }
    if (includeIds.length) p.set("include", includeIds.join(","));
    if (excludeIds.length) p.set("exclude", excludeIds.join(","));
    const s = p.toString();
    return s ? `?${s}` : "";
  }, [q, includeIds, excludeIds]);

  // fetch recipes when filters change and implement filters in URL
  useEffect(() => {
    const t = setTimeout(async () => {
      try {
        if (qs) {
          window.history.replaceState(null, "", qs);
        } else {
          const url = window.location.pathname;
          window.history.replaceState(null, "", url);
        }

        const r = await fetch(`/api/recipes${qs}`);
        if (!r.ok) throw new Error(`recipes ${r.status}`);
        const data = await r.json();
        setRecipes(Array.isArray(data) ? data : []);
      } catch (e) {
        console.error("Error loading recipes", e);
        setRecipes([]);
      }
    }, 250);
    return () => clearTimeout(t);
  }, [qs]);

  // toggles: a tag can be in include or exclude or neither
  const toggleInclude = (tid) => {
    setExcludeIds(prev => prev.filter(x => x !== tid));
    setIncludeIds(prev => (prev.includes(tid) ? prev.filter(x => x !== tid) : [...prev, tid]));
  };

  const toggleExclude = (tid) => {
    setIncludeIds(prev => prev.filter(x => x !== tid));
    setExcludeIds(prev => (prev.includes(tid) ? prev.filter(x => x !== tid) : [...prev, tid]));
  };

  return (
    <div className="p-4 space-y-4">
      <h2 className="text-xl font-semibold">Search Recipes</h2>

      {/* search */}
      <input
        className="border p-2 rounded w-full max-w-lg"
        placeholder="Search by title, author, or steps…"
        value={q}
        onChange={e => setQ(e.target.value)}
      />

      {/* tag filters */}
      <div className="grid md:grid-cols-2 gap-4">
        <div>
          <div className="font-medium mb-2">Include tags (must have ANY):</div>
          <div className="flex flex-wrap gap-2">
            {tags.map(t => {
              const tid = t.id;
              const selected = includeIds.includes(tid);
              return (
                <button
                  key={tid}
                  type="button"
                  onClick={() => toggleInclude(tid)}
                  aria-pressed={selected}
                  className={[
                    "inline-flex items-center rounded-full px-2 py-1 text-sm border",
                    selected
                      ? "bg-green-100 text-green-900 border-green-600"
                      : "bg-white text-gray-800 border-gray-300 hover:bg-gray-50",
                  ].join(" ")}
                  title={`Include ${t.name}`}
                >
                  {selected && <span className="mr-1">✓</span>}
                  {t.name}
                </button>
              );
            })}
          </div>
        </div>
        <div>
          <div className="font-medium mb-2">Exclude tags (must NOT have):</div>
          <div className="flex flex-wrap gap-2">
            {tags.map(t => {
              const tid = t.id;
              const selected = excludeIds.includes(tid);
              return (
                <button
                  key={tid}
                  type="button"
                  onClick={() => toggleExclude(tid)}
                  aria-pressed={selected}
                  className={[
                    "inline-flex items-center rounded-full px-2 py-1 text-sm border",
                    selected
                      ? "bg-red-100 text-red-900 border-red-600"
                      : "bg-white text-gray-800 border-gray-300 hover:bg-gray-50",
                  ].join(" ")}
                  title={`Exclude ${t.name}`}
                >
                  {selected && <span className="mr-1">✕</span>}
                  {t.name}
                </button>
              );
            })}
          </div>
        </div>
      </div>

      {/* results */}
      <div
        className="grid gap-3"
        style={{ gridTemplateColumns: "repeat(auto-fill, minmax(220px, 1fr))" }}
      >
        {recipes.map(r => (
          <Link key={r.recipeId} to={`/recipes/${r.recipeId}`} className="no-underline text-inherit">
            <div className="border rounded p-3 bg-white">
              <div className="font-semibold">{r.title}</div>
              <div className="text-sm text-gray-600">
                {(r.makeTime ?? "—")} min {r.createdByName ? `· by ${r.createdByName}` : ""}
              </div>
            </div>
          </Link>
        ))}
        {recipes.length === 0 && (
          <div className="text-gray-500">No recipes match your filters.</div>
        )}
      </div>
    </div>
  );
}
