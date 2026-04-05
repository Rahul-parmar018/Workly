"use client";

import React, { useState, useEffect } from "react";
import { collection, onSnapshot, doc, updateDoc, deleteDoc } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { 
  Briefcase, 
  Search, 
  Filter, 
  CheckCircle2, 
  XCircle, 
  Mail,
  MoreHorizontal,
  Clock,
  ShieldCheck,
  UserX
} from "lucide-react";
import { cn } from "@/lib/utils";

export default function ProvidersPage() {
  const [providers, setProviders] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    const unsubscribe = onSnapshot(collection(db, "providers"), (snapshot) => {
      const data = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setProviders(data);
      setLoading(false);
    }, (error) => {
      console.error("Error fetching providers:", error);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleApproval = async (id: string, approve: boolean) => {
    const action = approve ? "verify" : "put on hold";
    if (!confirm(`Are you sure you want to ${action} this provider?`)) return;
    try {
      await updateDoc(doc(db, "providers", id), {
        isApproved: approve
      });
      // Optionally update user role to 'provider' in the users collection too if needed, 
      // but usually the role is already set to provider when they register as one.
    } catch (error) {
      console.error("Error updating provider status:", error);
    }
  };

  const handleRemoveProvider = async (id: string) => {
    if (!confirm("Are you sure you want to REMOVE this provider from the network?")) return;
    try {
      await deleteDoc(doc(db, "providers", id));
      // Revert user role to 'user'
      await updateDoc(doc(db, "users", id), { role: "user" });
    } catch (err) {
       alert("Failed to remove provider");
    }
  }

  const filteredProviders = providers.filter(p => 
    p.name?.toLowerCase().includes(searchTerm.toLowerCase()) || 
    p.email?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    p.service?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Providers Network</h1>
          <p className="text-sm font-medium text-text-muted mt-1">Manage professional credentials and approval states.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
            <input 
              type="text" 
              placeholder="Search providers..." 
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
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Professional</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Status</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Domain</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted text-right">Verification</th>
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
                    <td className="px-6 py-4"><div className="h-6 w-20 bg-border-color/50 rounded-full" /></td>
                    <td className="px-6 py-4"><div className="h-4 w-24 bg-border-color/50 rounded" /></td>
                    <td className="px-6 py-4 text-right"><div className="h-9 w-24 bg-border-color/50 rounded-lg ml-auto" /></td>
                  </tr>
                ))
              ) : filteredProviders.length === 0 ? (
                <tr>
                  <td colSpan={4} className="px-6 py-20 text-center text-text-muted font-medium">No providers registered yet.</td>
                </tr>
              ) : (
                filteredProviders.map((provider) => (
                  <tr key={provider.id} className="hover:bg-bg-body/40 transition-colors group">
                    <td className="px-6 py-4">
                      <div className="flex items-center gap-3">
                        <div className="w-10 h-10 rounded-xl bg-maxton-purple/10 flex items-center justify-center text-maxton-purple border border-maxton-purple/20 group-hover:scale-110 transition-transform">
                          <Briefcase className="w-5 h-5" />
                        </div>
                        <div>
                          <p className="text-sm font-bold text-text-primary">{provider.name || "Anon Professional"}</p>
                          <div className="flex items-center gap-1.5 text-xs font-medium text-text-muted">
                            <Mail className="w-3 h-3" />
                            {provider.email || "No email info"}
                          </div>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      {provider.isApproved ? (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide bg-green-500/10 text-green-500 border border-green-500/20 shadow-sm">
                          <ShieldCheck className="w-3 h-3" />
                          Verified
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide bg-yellow-500/10 text-yellow-500 border border-yellow-500/20 shadow-sm">
                          <Clock className="w-3 h-3" />
                          Pending Review
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-xs font-bold text-text-secondary uppercase tracking-tight">{provider.service || "Generalist"}</span>
                    </td>
                    <td className="px-6 py-4 text-right">
                       <div className="flex items-center justify-end gap-2">
                        {provider.isApproved ? (
                          <button 
                            onClick={() => handleApproval(provider.id, false)}
                            className="p-2 text-maxton-red hover:bg-maxton-red/10 rounded-xl transition-colors"
                            title="Revoke Verification"
                          >
                            <XCircle className="w-5 h-5" />
                          </button>
                        ) : (
                          <button 
                            onClick={() => handleApproval(provider.id, true)}
                            className="p-2 text-maxton-green hover:bg-maxton-green/10 rounded-xl transition-colors"
                            title="Approve Provider"
                          >
                            <CheckCircle2 className="w-5 h-5" />
                          </button>
                        )}
                        <button 
                          onClick={() => handleRemoveProvider(provider.id)}
                          className="p-2 text-text-muted hover:text-maxton-red hover:bg-maxton-red/10 rounded-xl transition-colors"
                          title="Remove from Network"
                        >
                          <UserX className="w-5 h-5" />
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
