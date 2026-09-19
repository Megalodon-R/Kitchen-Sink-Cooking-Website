import { useState } from "react";

// this is the page where admins can add recipes.
// There are requirements for specific items that need to be within the form.
export default function AdminAddRecipePage() {
  // the form itself that the user adds inputs to.
  const [form, setForm] = useState({
    title: "", 
    makeTime: "", 
    amountage: "", 
    steps: "", 
    tagsInput: ""
  });

  // gets rid of spaces and splits tags by commas
  // so we have just the strings as an indiv object.
  const parseTags = (s) =>
    Array.from(new Set(
      (s || "")
        .split(",")
        .map(t => t.trim())
        .filter(Boolean)
        .map(t => t.toLowerCase())
    ));

  const submit = async (e) => {
    e.preventDefault();

    const tagNames = parseTags(form.tagsInput);
    // ensures at least one tag is put in
    if (tagNames.length === 0) return alert("Please add at least one tag.");

    // this is the form logistics
    const res = await fetch("/api/admin/recipes", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      credentials: "include",
      body: JSON.stringify({
        title: form.title,
        makeTime: form.makeTime ? Number(form.makeTime) : null,
        amountage: form.amountage || null,
        steps: form.steps || "",
        tagNames: parseTags(form.tagsInput)
      })
    });
    
    const txt = await res.text().catch(() => "");
    if (!res.ok) return alert(`Create failed: ${res.status} ${txt}`);

    alert("Recipe created");
    setForm({ title: "", makeTime: "", amountage: "", steps: "", tagsInput: "" });
  };

  return (
    // this is formatting and directions for the form
    <form onSubmit={submit} className="p-6 max-w-xl mx-auto space-y-3">
      <input className="border p-2 w-full" placeholder="Title"
             value={form.title}
             onChange={e=>setForm(f=>({...f,title:e.target.value}))}
             required /> {/* ensures title is required */}

      <input className="border p-2 w-full" placeholder="Make time (minutes)"
             value={form.makeTime}
             onChange={e=>setForm(f=>({...f,makeTime:e.target.value}))} />

      <input className="border p-2 w-full" placeholder="Amountage"
             value={form.amountage}
             onChange={e=>setForm(f=>({...f,amountage:e.target.value}))} />

      <textarea className="border p-2 w-full" placeholder="Steps"
                value={form.steps}
                onChange={e=>setForm(f=>({...f,steps:e.target.value}))}
                required /> {/* ensures steps are required */}

      <input className="border p-2 w-full"
             placeholder="Tags (comma-separated: egg, dairy)"
             value={form.tagsInput}
             onChange={e=>setForm(f=>({...f,tagsInput:e.target.value}))}
             required /> {/* ensures tags are required */}

      <button className="bg-blue-600 text-white px-4 py-2 rounded">Add</button>
    </form>
  );
}
