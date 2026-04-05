"use client";

import React, { useState, useEffect } from "react";
import { collection, onSnapshot, doc, updateDoc, deleteDoc } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { 
  Users as UsersIcon, 
  Search, 
  Filter, 
  MoreHorizontal,
  Mail,
  UserCheck,
  ShieldAlert,
  Trash2,
  ShieldCheck,
  ShieldX
} from "lucide-react";
import { cn } from "@/lib/utils";

export default function UsersPage() {
  const [users, setUsers] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    const unsubscribe = onSnapshot(collection(db, "users"), (snapshot) => {
      const usersData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setUsers(usersData);
      setLoading(false);
    }, (error) => {
      console.error("Error fetching users:", error);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleRoleChange = async (userId: string, newRole: string) => {
    if (!confirm(`Are you sure you want to change this user's role to ${newRole}?`)) return;
    try {
      await updateDoc(doc(db, "users", userId), { role: newRole });
    } catch (err) {
       alert("Failed to update role");
    }
  };

  const handleDeleteUser = async (userId: string) => {
    if (!confirm("Are you sure you want to PERMANENTLY delete this user? This cannot be undone.")) return;
    try {
      await deleteDoc(doc(db, "users", userId));
    } catch (err) {
      alert("Failed to delete user");
    }
  };

  const filteredUsers = users.filter(user => 
    (user.name?.toLowerCase().includes(searchTerm.toLowerCase()) || 
     user.email?.toLowerCase().includes(searchTerm.toLowerCase()))
  );

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Users Management</h1>
          <p className="text-sm font-medium text-text-muted mt-1">Manage system access and privileges.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
            <input 
              type="text" 
              placeholder="Search users..." 
              value={searchTerm}
              onChange={(e) => setSearchTerm(e.target.value)}
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
              ) : filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-20 text-center text-text-muted font-medium">No users found.</td>
                </tr>
              ) : (
                filteredUsers.map((user) => (
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
                      <div className="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                        {user.role === "admin" ? (
                          <button 
                            onClick={() => handleRoleChange(user.id, "user")}
                            className="p-2 text-maxton-blue hover:bg-maxton-blue/10 rounded-xl transition-colors"
                            title="Demote to User"
                          >
                            <ShieldX className="w-4 h-4" />
                          </button>
                        ) : (
                          <button 
                            onClick={() => handleRoleChange(user.id, "admin")}
                            className="p-2 text-maxton-green hover:bg-maxton-green/10 rounded-xl transition-colors"
                            title="Promote to Admin"
                          >
                            <ShieldCheck className="w-4 h-4" />
                          </button>
                        )}
                        <button 
                          onClick={() => handleDeleteUser(user.id)}
                          className="p-2 text-maxton-red hover:bg-maxton-red/10 rounded-xl transition-colors"
                          title="Delete User"
                        >
                          <Trash2 className="w-4 h-4" />
                        </button>
                      </div>
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
