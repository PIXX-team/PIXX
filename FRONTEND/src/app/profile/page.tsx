"use client";

import Inquiry from "@/components/login/Inquiry";
import Logout from "@/components/login/Logout";
import DeleteAccount from "@/components/login/DeleteAccount";

export default function Page() {
  return (
    <div className="flex flex-col gap-4 p-4">
      <Inquiry />
      <Logout />
      <DeleteAccount />
    </div>
  );
}
