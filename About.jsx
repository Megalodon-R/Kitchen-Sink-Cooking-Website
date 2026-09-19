import Megan from "../assets/Megan.PNG";
import MyAnnia from "../assets/MyAnnia.JPG";
import Nathan from "../assets/Nathan.jpg";
import Sophie from "../assets/Sophie.JPG";
import JR from "../assets/IMG_4407.JPG";

// this is our about page. It is the page that introduces all of our members
// and then tells us a bit about them and the company.
export default function AboutText() {
  return (
    <>
      <div className="flex flex-col h-screen w-screen bg-purple-50 overscroll-x-none p-15 items-center">
        <div className="p-5 pl-40 pr-40 bg-purple-50">
          <div className="font-titanOne justify-items-center bg-purple-200 pt-5 pb-5  rounded-xl">
            <h1 className="text-black text-7xl font-titanOne w-250 text-center">
              The SWIVEL Company
            </h1>
          </div>
          <div className="pt-10 justify-items-center">
            <h1 className="text-2xl font-titanOne text-center w-200">
              When college student Megan Robie found herself scrolling Pinterest,
              struggling to find a meal to make with the few ingredients she had
              on hand, she thought to herself...
            </h1>
          </div>
          <div className="mt-10 justify-items-center bg-purple-200 p-4 rounded-xl">
            <h1 className="text-5xl font-titanOne text-center">
              "There MUST be another way!"
            </h1>
          </div>
          <div className="pt-10 justify-items-center">
            <h1 className="text-2xl font-titanOne text-center w-200">
              Megan, along with 4 of her friends, would soon found SWIVEL, a
              company focused on solving everyday college problems through
              innovative technology solutions. Their first product, KitchenSink,
              aims to revolutionize the way people find and share recipes based
              online.
            </h1>
          </div>
          <hr className="mt-8" />
          <div className="font-titanOne justify-items-center bg-purple-200 mt-10 pt-5 pb-5  rounded-xl">
            <h1 className="text-black text-7xl font-titanOne w-250 text-center">
              Meet the Team!
            </h1>
          </div>
          <div className="flex font-titanOne justify-items-center mt-5 pt-5 pb-5 ">
            <img
              src={Megan}
              alt="MeganRobieImg"
              className=" size-50 bg-amber-300"
            ></img>
            <div className="flex flex-col ml-4 text-left justify-around">
              <h1 className="text-black text-4xl font-titanOne w-250">
                Megan Robie
              </h1>
              <h1 className="text-xl font-titanOne w-200">
                Megan Robie is a sophmore at Miami University, and is the Project Manager and Data Layer Specialist for Swivel. She is a jack of all trades with a love of reading and the arts. 
              </h1>
            </div>
          </div>
          <div className="flex font-titanOne justify-items-center mt-5 pt-5 pb-5 ">
            <img
              src={Nathan}
              alt="NathanLomnickyImg"
              className=" size-50 bg-green-300"
            ></img>
            <div className="flex flex-col ml-4 text-left justify-around">
              <h1 className="text-black text-4xl font-titanOne w-250">
                Nathan Lomnicky
              </h1>
              <h1 className="text-xl font-titanOne w-200">
                Nathan Lomnicky is a Sophomore at Miami University, and is the Technical Manager for SWIVEL. He understands the affordances given to him, and seeks to help others who are struggling in any way he can.
              </h1>
            </div>
          </div>
          <div className="flex font-titanOne justify-items-center mt-5 pt-5 pb-5 ">
            <img
              src={Sophie}
              alt="SophieMcIssacImg"
              className=" size-50 bg-amber-300"
            ></img>
            <div className="flex flex-col ml-4 text-left justify-around">
              <h1 className="text-black text-4xl font-titanOne w-250">
                Sophie McIssac
              </h1>
              <h1 className="text-xl font-titanOne w-200">
                Sophie is an Accounting and Computer Science double major at Miami University, and is a Programmer and Tester for SWIVEL. 
              </h1>
            </div>
          </div>
          <div className="flex font-titanOne justify-items-center mt-5 pt-5 pb-5 ">
            <img
              src={MyAnnia}
              alt="MyAnniaLewisImg"
              className=" size-50 bg-green-300"
            ></img>
            <div className="flex flex-col ml-4 text-left justify-around">
              <h1 className="text-black text-4xl font-titanOne w-250">
                My'Annia Lewis
              </h1>
              <h1 className="text-xl font-titanOne w-200">
                My'Annia is a Junior Computer Science student at Miami University, and is one of the Product Developers at Swivel. Once a broke college student herself, she seeks to help other students find meals easy for them.
              </h1>
            </div>
          </div>
          <div className="flex font-titanOne justify-items-center mt-5 pt-5 pb-5 ">
            <img
              src={JR}
              alt="JackRollsImg"
              className=" size-50 bg-amber-300"
            ></img>
            <div className="flex flex-col ml-4 text-left justify-around">
              <h1 className="text-black text-4xl font-titanOne w-250">
                Jack Rolls
              </h1>
              <h1 className="text-xl font-titanOne w-200">
                Jack is a Junior Computer Science student at Miami University, and one of the Product Developers at SWIVEL
              </h1>
            </div>
          </div>
        </div>
      </div>
    </>
  );
}