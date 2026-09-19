import { useEffect, useMemo, useState } from "react";
import { useParams } from "react-router-dom";
import { useAuth } from "../auth/AuthContext";

// this is the RecipeDetailPage, its the recipe itself that you see
// when you click on the recipe icon. It's for display and comments.

// saved/liked endpoints
const likeApi = {
  add:   (id) => `/api/recipes/${id}/save`, // POST
  remove:(id) => `/api/recipes/${id}/save`, // DELETE
  list:  ()  => `/api/me/saved`,            // GET -> array with recipeId
  label: { on: "Saved ✓", off: "Save" }
};

const isNum = (v) => typeof v === "number" && Number.isFinite(v);
const first = (...xs) => xs.find((v) => v !== undefined && v !== null);

export default function RecipeDetailPage() {
  const { id } = useParams();
  const recipeId = Number(id);
  const { me } = useAuth();
  const myId = (me) => Number(me?.userId ?? me?.id);
  const commentOwnerId = (c) => Number(c?.userId ?? c?.user_id ?? c?.authorId ?? c?.author_id);
  const isStaff = (me) => Number(me?.clearance) >= 1;

  const [recipe, setRecipe] = useState(null);
  const [comments, setComments] = useState([]);
  const [message, setMessage] = useState("");
  const [rating, setRating] = useState(5);
  const [loading, setLoading] = useState(true);

  const [isSaved, setIsSaved] = useState(false);
  const [saving, setSaving] = useState(false);

  // anyone who is a user can delete their own comment
  const canDeleteComment = (c) => {
  if (!me) return false;
  const owner = myId(me) && commentOwnerId(c) && myId(me) === commentOwnerId(c);
  return owner || isStaff(me);
  };

  useEffect(() => {
    let alive = true;
    (async () => {
      try {
        setLoading(true);

        // recipe (expects: title, description, amountage, steps)
        const r1 = await fetch(`/api/recipes/${recipeId}`, { credentials: "include" });
        if (r1.ok) {
          const data = await r1.json();
          if (alive) setRecipe(data);
        } else if (alive) setRecipe(null);

        // saved/liked: Adds whether this recipe is save or not to your user identity
        try {
          const rList = await fetch(likeApi.list(), { credentials: "include" });
          if (alive) {
            if (rList.ok) {
              const arr = await rList.json();
              const saved = Array.isArray(arr) && arr.some(x => Number(first(x?.recipeId, x?.id)) === recipeId);
              setIsSaved(saved);
            } else setIsSaved(false);
          }
        } catch { /* ignore */ }

        // comments that are attached to the recipe page.
        const r2 = await fetch(`/api/recipes/${recipeId}/comments`, { credentials: "include" });
        if (r2.ok) {
          const raw = await r2.json();
          if (alive) setComments(Array.isArray(raw) ? raw : []);
        } else if (alive) setComments([]);
      } catch (e) {
        console.error(e);
        if (alive) { setRecipe(null); setComments([]); }
      } finally {
        if (alive) setLoading(false);
      }
    })();
    return () => { alive = false; };
  }, [recipeId, me?.userId]);

  // this is for when a comment is posted.
  // There's security permissions to ensure you're logged in.
  const submitComment = async (e) => {
    e.preventDefault();
    if (!me) { alert("Please log in to comment."); return; }
    const body = { message, rating };
    const res = await fetch(`/api/recipes/${recipeId}/comments`, {
      method: "POST",
      credentials: "include",
      headers: { "Content-Type": "application/json", Accept: "application/json" },
      body: JSON.stringify(body),
    });
    if (!res.ok) {
      const text = await res.text();
      alert(`Failed to post comment: ${text}`);
      return;
    }
    setMessage("");
    const r2 = await fetch(`/api/recipes/${recipeId}/comments`, { credentials: "include" });
    const raw = r2.ok ? await r2.json() : [];
    setComments(Array.isArray(raw) ? raw : []);
  };

  // This is deleting a comment which involves some permissions
  // i.e whether you're an admin/mod or its your own comment (can delete)
  // or whether its not your comment and you don't have permission
  const deleteComment = async (commentId) => {
    if (!isNum(commentId)) { alert("Invalid comment id."); return; }
    if (!confirm("Delete this comment?")) return;
    const res = await fetch(`/api/comments/${commentId}`, {
      method: "DELETE",
      credentials: "include", // send JWT cookie
      headers: { Accept: "application/json" },
    });
    if (!res.ok) { // permission if you or given status
      const text = await res.text().catch(() => "");
      alert(`Delete failed (${res.status}). ${text || ""}`);
      return;
    }
    setComments((prev) => prev.filter((c) => Number(first(c?.commentId, c?.id, c?.comment_id)) !== commentId));
  };

  // Only allows logged in users to save a recipe
  // whether or not save is active
  const toggleSave = async () => {
    if (!me) { alert("Please log in to save recipes."); return; }
    setSaving(true);
    try {
      const url = isSaved ? likeApi.remove(recipeId) : likeApi.add(recipeId);
      const method = isSaved ? "DELETE" : "POST";
      const res = await fetch(url, {
        method,
        credentials: "include",
        headers: { Accept: "application/json", "Content-Type": "application/json" },
        body: method === "POST" ? "{}" : undefined
      });
      if (res.ok || [200,201,204].includes(res.status)) {
        setIsSaved((s) => !s);
      } else if (res.status === 401) {
        alert("Please log in.");
      } else {
        const text = await res.text().catch(() => "");
        alert(`Save toggle failed (${res.status}). ${text}`);
      }
    } finally {
      setSaving(false);
    }
  };

  // this is the average rating calculated based upon given comment ratings.
  const avgRating = useMemo(() => {
    if (!comments.length) return 0;
    const sum = comments.reduce((a, c) => a + (Number(c?.rating ?? c?.stars ?? 0) || 0), 0);
    return Math.round((sum / comments.length) * 10) / 10;
  }, [comments]);

  if (loading) return <div className="p-6">Loading…</div>;
  if (!recipe) return <div className="p-6">Recipe not found.</div>;

  const title = recipe.title ?? "Recipe";
  const description = recipe.description;
  const amountage = typeof recipe.amountage === "string" ? recipe.amountage : "";
  const steps = typeof recipe.steps === "string" ? recipe.steps : "";

  return (
    // formatting and such
    <div className="p-6 max-w-3xl mx-auto">
      {/* Header */}
      <div className="flex items-start justify-between gap-3">
        <div>
          <h1 className="text-3xl md:text-4xl font-extrabold tracking-tight mb-2">{title}</h1>
          <div className="text-sm text-gray-600 mb-4">
            {/* add any meta like time/author if you have them */}
          </div>
        </div>
        <button
          onClick={toggleSave}
          disabled={saving}
          className="rounded px-3 py-1 border"
          title={isSaved ? "Unsave" : "Save"}
        >
          {saving ? "…" : isSaved ? likeApi.label.on : likeApi.label.off}
        </button>
      </div>

      {/* Description */}
      {description && (
        <section className="prose max-w-none mb-4">
          <p>{description}</p>
        </section>
      )}

      {/* Amountage (string; preserve formatting) */}
      {amountage.trim() && (
        <section className="mb-6">
          <h2 className="text-xl font-semibold mb-2">Ingredients</h2>
          <pre className="whitespace-pre-wrap">{amountage}</pre>
        </section>
      )}

      {/* Steps (string; preserve formatting) */}
      {steps.trim() && (
        <section className="mb-6">
          <h2 className="text-xl font-semibold mb-2">Steps</h2>
          <pre className="whitespace-pre-wrap">{steps}</pre>
        </section>
      )}

      {/* List of comments and their info*/}
      {/* permissions for deleting comments implemented*/}
      <section className="mb-6">
        <h2 className="text-xl font-semibold mb-2">Comments</h2>
        {comments.length === 0 && <div className="text-sm text-gray-600">No comments yet.</div>}
        <ul className="divide-y">
          {comments.map((c) => {
            const cid = Number(first(c?.commentId, c?.id, c?.comment_id));
            const username = first(c?.username, c?.userName, c?.authorName, c?.author_name, "user");
            const ratingVal = Number(first(c?.rating, c?.stars, 0)) || 0;
            return (
              <li key={String(cid)} className="py-3">
                <div className="flex items-center justify-between">
                  <div className="font-semibold text-base">
                    <strong>{username}</strong>
                    <span className="mx-2">·</span>
                    <strong>{ratingVal}/5</strong>
                  </div>
                  {canDeleteComment(c) && (
                    <button
                      className="text-red-600 text-sm"
                      disabled={!isNum(cid)}
                      onClick={() => deleteComment(cid)}
                    >
                      Delete
                    </button>
                  )}
                </div>
                <p className="mt-1">{c?.message ?? c?.text ?? c?.body ?? ""}</p>
                {c?.createdAt && <div className="text-xs text-gray-500 mt-1">{String(c.createdAt)}</div>}
              </li>
            );
          })}
        </ul>
      </section>

      {/* new comment implementation*/}
      <section>
        <h3 className="text-lg font-semibold mb-2">Add a comment</h3>
        <form onSubmit={submitComment} className="space-y-3">
          <textarea
            className="w-full border rounded p-2"
            rows={4}
            value={message}
            onChange={(e) => setMessage(e.target.value)}
            placeholder="Write your thoughts…"
          />
          <div className="flex items-center gap-3">
            <label className="text-sm">Rating:</label>
            <select
              className="border rounded p-1"
              value={rating}
              onChange={(e) => setRating(Number(e.target.value))}
            >
              {[1,2,3,4,5].map(n => <option key={n} value={n}>{n}</option>)}
            </select>
            <button className="bg-blue-600 text-white px-4 py-2 rounded" type="submit">
              Post
            </button>
          </div>
          {!me && <div className="text-sm text-gray-500">Log in to comment.</div>}
        </form>
      </section>
    </div>
  );
}