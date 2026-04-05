"use client";

import React, { useState, useEffect } from "react";
import { collection, onSnapshot, doc, updateDoc } from "firebase/firestore";
import { db } from "@/lib/firebase";
import { 
  CalendarCheck, 
  Search, 
  Filter, 
  MoreHorizontal,
  Clock,
  CheckCircle2,
  XCircle,
  User,
  Briefcase,
  ExternalLink,
  IndianRupee
} from "lucide-react";

export default function BookingsPage() {
  const [bookings, setBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);
  const [searchTerm, setSearchTerm] = useState("");

  useEffect(() => {
    // Using 'orders' for consistency with the Android app's primary transaction collection
    const unsubscribe = onSnapshot(collection(db, "orders"), (snapshot) => {
      const bookingsData = snapshot.docs.map(doc => ({
        id: doc.id,
        ...doc.data()
      }));
      setBookings(bookingsData);
      setLoading(false);
    }, (error) => {
      console.error("Error fetching bookings:", error);
      setLoading(false);
    });

    return () => unsubscribe();
  }, []);

  const handleStatusChange = async (bookingId: string, newStatus: string) => {
    if (!confirm(`Force mark this booking as ${newStatus}?`)) return;
    try {
      await updateDoc(doc(db, "orders", bookingId), { status: newStatus });
    } catch (err) {
      alert("Failed to update status");
    }
  };

  const filteredBookings = bookings.filter(b => 
    b.id.toLowerCase().includes(searchTerm.toLowerCase()) || 
    b.userName?.toLowerCase().includes(searchTerm.toLowerCase()) ||
    b.serviceName?.toLowerCase().includes(searchTerm.toLowerCase())
  );

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Bookings Overview</h1>
          <p className="text-sm font-medium text-text-muted mt-1">Live transaction monitoring and status control.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
            <input 
              type="text" 
              placeholder="Search by ID, User, or Service..." 
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
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Transaction ID</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Status</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Identities</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Revenue</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted text-right">Actions</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-border-color">
              {loading ? (
                Array.from({ length: 5 }).map((_, i) => (
                  <tr key={i} className="animate-pulse">
                    <td className="px-6 py-4"><div className="h-4 w-24 bg-border-color/50 rounded" /></td>
                    <td className="px-6 py-4"><div className="h-6 w-20 bg-border-color/50 rounded-full" /></td>
                    <td className="px-6 py-4"><div className="h-4 w-32 bg-border-color/50 rounded" /></td>
                    <td className="px-6 py-4"><div className="h-4 w-20 bg-border-color/50 rounded" /></td>
                    <td className="px-6 py-4 text-right"><div className="h-8 w-8 bg-border-color/50 rounded-lg ml-auto" /></td>
                  </tr>
                ))
              ) : filteredBookings.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-20 text-center text-text-muted font-medium">No bookings matching filter.</td>
                </tr>
              ) : (
                filteredBookings.map((booking) => (
                  <tr key={booking.id} className="hover:bg-bg-body/40 transition-colors group">
                    <td className="px-6 py-4">
                       <span className="font-mono text-[10px] text-text-muted uppercase bg-bg-body px-2 py-1 rounded-md border border-border-color">{booking.id.substring(0, 8)}</span>
                       <p className="text-[10px] font-bold text-text-secondary mt-1 uppercase tracking-tighter">{booking.serviceName || "Global Service"}</p>
                    </td>
                    <td className="px-6 py-4">
                      {booking.status === "completed" ? (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide bg-green-500/10 text-green-500 border border-green-500/20 shadow-sm">
                          <CheckCircle2 className="w-3 h-3" />
                          Completed
                        </span>
                      ) : (booking.status === "pending" || !booking.status) ? (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide bg-yellow-500/10 text-yellow-500 border border-yellow-500/20 shadow-sm">
                          <Clock className="w-3 h-3" />
                          Pending
                        </span>
                      ) : (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide bg-maxton-red/10 text-maxton-red border border-maxton-red/20 shadow-sm">
                          <XCircle className="w-3 h-3" />
                          {booking.status}
                        </span>
                      )}
                    </td>
                    <td className="px-6 py-4">
                      <div className="flex flex-col gap-1.5">
                        <div className="flex items-center gap-2 text-[10px] font-bold text-text-secondary uppercase tracking-tight">
                          <User className="w-3 h-3 text-maxton-blue" />
                          {booking.userName || "Unknown User"}
                        </div>
                        <div className="flex items-center gap-2 text-[10px] font-bold text-text-secondary uppercase tracking-tight">
                          <Briefcase className="w-3 h-3 text-maxton-purple" />
                          {booking.providerName || "Assigned Pro"}
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                       <div className="flex items-center gap-1 text-sm font-black text-text-primary">
                          <IndianRupee className="w-3 h-3 text-green-500" />
                          {booking.finalPrice?.toLocaleString() || "0"}
                       </div>
                       <p className="text-[10px] text-text-muted font-medium">{booking.paymentMethod || "On-site"}</p>
                    </td>
                    <td className="px-6 py-4 text-right">
                       <div className="flex items-center justify-end gap-2 opacity-0 group-hover:opacity-100 transition-opacity">
                         {booking.status !== "completed" && (
                           <button 
                             onClick={() => handleStatusChange(booking.id, "completed")}
                             className="p-2 text-maxton-green hover:bg-maxton-green/10 rounded-xl transition-colors"
                             title="Mark Completed"
                           >
                             <CheckCircle2 className="w-4 h-4" />
                           </button>
                         )}
                         {booking.status !== "cancelled" && (
                           <button 
                             onClick={() => handleStatusChange(booking.id, "cancelled")}
                             className="p-2 text-maxton-red hover:bg-maxton-red/10 rounded-xl transition-colors"
                             title="Cancel Order"
                           >
                             <XCircle className="w-4 h-4" />
                           </button>
                         )}
                         <button className="p-2 text-text-muted hover:bg-bg-body rounded-xl border border-transparent hover:border-border-color transition-all">
                           <MoreHorizontal className="w-4 h-4" />
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
