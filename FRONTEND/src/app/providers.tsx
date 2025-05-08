// app/providers.tsx
// React Query 적용을 위한 provider

"use client";

import { QueryClient, QueryClientProvider } from "@tanstack/react-query";
import { ReactNode, useState } from "react";

interface ProvidersProps {
  children: ReactNode;
  className?: string;
}

export default function Providers({ children, className }: ProvidersProps) {
  const [queryClient] = useState(() => new QueryClient());

  return (
    <QueryClientProvider client={queryClient}>
      <div className={className}>{children}</div>
    </QueryClientProvider>
  );
}
