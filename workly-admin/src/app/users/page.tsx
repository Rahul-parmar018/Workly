"use client";

import React, { useState, useEffect } from "react";
import { collection, getDocs } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { 
  Users as UsersIcon, 
  Search, 
  Filter, 
  MoreHorizontal,
  Mail,
  UserCheck,
  ShieldAlert
} from "lucide-react";
import { cn } from "@/lib/utils";

export default function UsersPage() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchUsers = async () => {
      try {
        const usersSnap = await getDocs(collection(db, "users"));
        const usersData = usersSnap.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        }));
        setUsers(usersData);
      } catch (error) {
        console.error("Error fetching users:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchUsers();
  }, []);

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Users Management</h1>
          <p className="text-sm font-medium text-text-muted mt-1">Manage and view all registered users from Firestore.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
            <input 
              type="text" 
              placeholder="Search users..." 
              className="bg-bg-surface border border-border-color rounded-xl pl-10 pr-4 py-2 text-sm text-text-primary focus:outline-none focus:border-maxton-blue transition-all w-64 shadow-sm"
            />
          </div>
          <button className="p-2 bg-bg-surface border border-border-color rounded-xl hover:bg-bg-body transition-colors shadow-sm">
            <Filter className="w-4 h-4 text-text-secondary" />
          </button>
        </div>
      </div>

      <div className="bg-bg-surface border border-border-color rounded-2xl overflow-hidden shadow-sm">
        <div className="overflow-x-auto">
          <table className="w-full text-left border-collapse">
            <thead>
              <tr className="bg-bg-body/50 border-b border-border-color">
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">User</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Role</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Status</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-color">
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-border-color/50" />
                        <div className="space-y-2">
                          <div className="h-4 w-32 bg-border-color/50 rounded" />
                          <div className="h-3 w-48 bg-border-color/50 rounded" />
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4"><div className="h-4 w-16 bg-border-color/50 rounded" /></td>
                    <td className="px-6 py-4"><div className="h-6 w-20 bg-border-color/50 rounded-full" /></td>
                    <td className="px-6 py-4 text-right"><div className="h-8 w-8 bg-border-color/50 rounded-lg ml-auto" /></td>
                  </tr>
                ))
              ) : users.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-20 text-center text-text-muted font-medium">No users found in the database.</td>
                </tr>
              ) : (
                users.map((user) => (
                  <tr key={user.id} className="hover:bg-bg-body/40 transition-colors group">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-maxton-blue/10 flex items-center justify-center text-maxton-blue border border-maxton-blue/20 group-hover:scale-110 transition-transform">
                          <UsersIcon className="w-5 h-5" />
                        </div>
                        <div>
                          <p className="text-sm font-bold text-text-primary">{user.name || "Unknown User"}</p>
                          <div className="flex items-center gap-1.5 text-xs font-medium text-text-muted">
                            <Mail className="w-3 h-3" />
                            {user.email || "No email provided"}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-2">
                        {user.role === "admin" ? (
                          <ShieldAlert className="w-4 h-4 text-maxton-pink" />
                        ) : (
                          <UserCheck className="w-4 h-4 text-maxton-blue" />
                        )}
                        <span className="text-xs font-bold uppercase tracking-tight text-text-secondary">{user.role || "User"}</span>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="inline-flex items-center px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide bg-green-500/10 text-green-500 border border-green-500/20">
                        Active
                      </span>
                    </td>
                    <td className="px-6 py-4 text-right">
                      <button className="p-2 text-text-muted hover:text-text-primary hover:bg-bg-body rounded-xl border border-transparent hover:border-border-color transition-all">
                        <MoreHorizontal className="w-5 h-5" />
                      </button>
                    </td>
                  </tr>
                ))
              )}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
}
