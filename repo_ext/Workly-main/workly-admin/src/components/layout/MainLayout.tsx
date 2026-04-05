import { Sidebar } from "./Sidebar";
import { Search, Bell, User } from "lucide-react";
import { ThemeToggle } from "@/components/theme-toggle";

export function MainLayout({ children }: { children: React.ReactNode }) {
  return (
    <div className="flex min-h-screen bg-bg-body text-text-primary transition-colors duration-300">
      <Sidebar />
      <div className="flex-1 ml-64 flex flex-col">
        {/* Header */}
        <header className="h-20 px-8 flex items-center justify-between sticky top-0 z-40 bg-bg-body/80 backdrop-blur-md border-b border-border-color transition-colors duration-300">
          <div className="flex items-center gap-4 bg-bg-surface border border-border-color rounded-2xl px-4 py-2 w-96 shadow-sm focus-within:shadow-md focus-within:border-maxton-blue transition-all">
            <Search className="w-5 h-5 text-text-muted" />
            <input 
              type="text" 
              placeholder="Search data..." 
              className="bg-transparent border-none outline-none text-sm w-full text-text-primary placeholder:text-text-muted"
            />
          </div>

          <div className="flex items-center gap-5">
            <ThemeToggle />
            <button className="relative w-10 h-10 flex items-center justify-center rounded-xl bg-bg-surface border border-border-color shadow-sm hover:shadow-md transition-all">
              <Bell className="w-5 h-5 text-text-secondary" />
              <div className="absolute top-2.5 right-2.5 w-2 h-2 bg-maxton-pink rounded-full border-2 border-bg-surface transition-colors" />
            </button>
            <div className="flex items-center gap-3 pl-4 border-l border-border-color transition-colors">
              <div className="text-right">
                <p className="text-xs font-bold text-text-primary">Rahul Parmar</p>
                <p className="text-[10px] text-text-secondary">Administrator</p>
              </div>
              <div className="w-10 h-10 rounded-xl bg-maxton-blue/10 flex items-center justify-center text-maxton-blue border border-maxton-blue/20">
                <User className="w-6 h-6" />
              </div>
            </div>
          </div>
        </header>

        {/* Page Content */}
        <main className="p-8 pb-12 animate-in fade-in slide-in-from-bottom-4 duration-1000">
          {children}
        </main>
      </div>
    </div>
  );
}
