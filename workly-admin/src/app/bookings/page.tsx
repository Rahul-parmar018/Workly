"use client";

import React, { useState, useEffect } from "react";
import { collection, getDocs } from "firebase/firestore";
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
  Briefcase
} from "lucide-react";

export default function BookingsPage() {
  const [bookings, setBookings] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchBookings = async () => {
      try {
        const bookingsSnap = await getDocs(collection(db, "bookings"));
        const bookingsData = bookingsSnap.docs.map(doc => ({
          id: doc.id,
          ...doc.data()
        }));
        setBookings(bookingsData);
      } catch (error) {
        console.error("Error fetching bookings:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchBookings();
  }, []);

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-700">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-text-primary">Bookings Overview</h1>
          <p className="text-sm font-medium text-text-muted mt-1">View and track all service bookings from Firestore.</p>
        </div>
        <div className="flex items-center gap-3">
          <div className="relative">
            <Search className="w-4 h-4 absolute left-3 top-1/2 -translate-y-1/2 text-text-muted" />
            <input 
              type="text" 
              placeholder="Search bookings..." 
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
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Booking ID</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Status</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Identities</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted">Date</th>
                <th className="px-6 py-4 text-[10px] font-bold uppercase tracking-widest text-text-muted text-right">Action</th>
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
              ) : bookings.length === 0 ? (
                <tr>
                  <td colSpan={5} className="px-6 py-20 text-center text-text-muted font-medium">No bookings found in the database.</td>
                </tr>
              ) : (
                bookings.map((booking) => (
                  <tr key={booking.id} className="hover:bg-bg-body/40 transition-colors group">
                    <td className="px-6 py-4 font-mono text-[10px] text-text-muted uppercase">{booking.id.substring(0, 8)}...</td>
                    <td className="px-6 py-4">
                      {booking.status === "completed" ? (
                        <span className="inline-flex items-center gap-1.5 px-2.5 py-1 rounded-full text-[10px] font-bold uppercase tracking-wide bg-green-500/10 text-green-500 border border-green-500/20 shadow-sm">
                          <CheckCircle2 className="w-3 h-3" />
                          Completed
                        </span>
                      ) : booking.status === "pending" || !booking.status ? (
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
                          User: <span className="text-text-muted font-mono">{booking.userId?.substring(0, 8) || "N/A"}...</span>
                        </div>
                        <div className="flex items-center gap-2 text-[10px] font-bold text-text-secondary uppercase tracking-tight">
                          <Briefcase className="w-3 h-3 text-maxton-purple" />
                          Provider: <span className="text-text-muted font-mono">{booking.providerId?.substring(0, 8) || "N/A"}...</span>
                        </div>
                      </div>
                    </td>
                    <td className="px-6 py-4">
                      <span className="text-[10px] font-bold text-text-secondary uppercase tracking-tighter">{booking.date || "No Date Set"}</span>
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
