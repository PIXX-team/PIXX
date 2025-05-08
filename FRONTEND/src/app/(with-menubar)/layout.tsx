//app/(with-menubar)/layout.tsx
import { ReactNode } from "react";
import Menubar from "@/components/Menubar";

export default function Layout({ children }: { children: ReactNode }) {
  return (
    <div>
      <Menubar />
      {children}
    </div>
  );
}
