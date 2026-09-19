import HomeText from "../HomeText.jsx";

// implements hometext so we can have a Page to reference.
// This is the homepage, makes a very pretty entry setup for when you first login or log out :)
export default function HomePage() {
  return (
    <div className="container mx-auto px-4 py-8 overflow-x-clip">
      <HomeText />
      <div className="mt-8 p-4">
      </div>
    </div>
  );
}
