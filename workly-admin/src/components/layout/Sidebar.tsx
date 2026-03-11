"use client";

import Link from "next/link";
import { usePathname } from "next/navigation";
import { 
  LayoutDashboard, 
  Users, 
  ClipboardList, 
  CalendarCheck, 
  Settings, 
  LogOut,
  ChevronRight
} from "lucide-react";
import { cn } from "@/lib/utils";

const menuItems = [
  { name: "Dashboard", icon: LayoutDashboard, href: "/" },
  { name: "Work Logs", icon: ClipboardList, href: "/work-logs" },
  { name: "Bookings", icon: CalendarCheck, href: "/bookings" },
  { name: "Users", icon: Users, href: "/users" },
  { name: "Settings", icon: Settings, href: "/settings" },
];

export function Sidebar() {
  const pathname = usePathname();

  return (
    <div className="w-64 h-screen border-r border-border-color fixed left-0 top-0 flex flex-col p-6 z-50 bg-bg-surface transition-colors duration-300">
      <div className="flex items-center gap-3 mb-10 pl-2">
        <div className="w-8 h-8 rounded-lg bg-gradient-to-br from-maxton-blue to-maxton-purple flex items-center justify-center text-white font-bold italic shadow-lg shadow-maxton-blue/20">
          W
        </div>
        <span className="text-xl font-bold tracking-tight text-text-primary">Workly Admin</span>
      </div>

      <nav className="flex-1 space-y-2">
        <div className="text-[10px] font-bold text-text-muted uppercase tracking-widest mb-4 pl-2 opacity-60">
          General
        </div>
        {menuItems.map((item) => {
          const isActive = pathname === item.href;
          return (
            <Link
              key={item.href}
              href={item.href}
              className={cn(
                "group flex items-center justify-between p-3 rounded-xl transition-all duration-300",
                isActive 
                  ? "bg-maxton-blue text-white shadow-lg shadow-maxton-blue/20" 
                  : "hover:bg-bg-body text-text-secondary hover:text-maxton-blue"
              )}
            >
              <div className="flex items-center gap-3">
                <item.icon className={cn("w-5 h-5", isActive ? "text-white" : "text-text-secondary group-hover:text-maxton-blue")} />
                <span className="font-medium text-sm">{item.name}</span>
              </div>
              {isActive && <div className="w-1.5 h-1.5 rounded-full bg-white shadow-[0_0_8px_white]" />}
            </Link>
          );
        })}
      </nav>

      <div className="mt-auto">
        <button className="flex items-center gap-3 p-3 w-full rounded-xl hover:bg-maxton-red/10 text-text-secondary hover:text-maxton-red transition-all duration-300">
          <LogOut className="w-5 h-5" />
          <span className="font-medium text-sm">Logout</span>
        </button>
      </div>
    </div>
  );
}
