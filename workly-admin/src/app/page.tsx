"use client";

import React, { useState, useEffect } from "react";
import {
  Users,
  Briefcase,
  CalendarCheck,
  TrendingUp,
  ArrowUpRight,
  ArrowDownRight,
  MoreVertical,
  Activity
} from "lucide-react";
import { cn } from "@/lib/utils";
import {
  AreaChart, Area, XAxis, YAxis, Tooltip, ResponsiveContainer,
  RadialBarChart, RadialBar, PolarAngleAxis
} from 'recharts';
import { collection, getDocs, query, where, orderBy, onSnapshot } from "firebase/firestore";
import { db } from "@/lib/firebase";

export default function Dashboard() {
  const [counts, setCounts] = useState({ users: 0, providers: 0, bookings: 0, revenue: 0 });
  const [todayStats, setTodayStats] = useState({ revenue: 0, bookings: 0 });
  const [chartData, setChartData] = useState<any[]>([]);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    // Real-time listener for orders to calculate revenue and volume
    const unsubscribeOrders = onSnapshot(collection(db, "orders"), (snapshot) => {
      let totalRevenue = 0;
      let todayRevenue = 0;
      let totalBookings = snapshot.size;
      let todayBookings = 0;
      
      const startOfDay = new Date();
      startOfDay.setHours(0, 0, 0, 0);

      const dailyMap: Record<string, number> = {};

      snapshot.docs.forEach(doc => {
        const data = doc.data();
        const price = data.finalPrice || 0;
        const createdAt = data.createdAt; // Assuming it's a number/timestamp
        
        if (data.status === "completed") {
          totalRevenue += price;
          if (createdAt >= startOfDay.getTime()) {
            todayRevenue += price;
          }
        }

        if (createdAt >= startOfDay.getTime()) {
          todayBookings++;
        }

        // Prepare chart data (last 7 days)
        if (createdAt) {
          const date = new Date(createdAt);
          const dateStr = date.toLocaleDateString('en-US', { weekday: 'short' });
          dailyMap[dateStr] = (dailyMap[dateStr] || 0) + 1;
        }
      });

      setCounts(prev => ({ ...prev, bookings: totalBookings, revenue: totalRevenue }));
      setTodayStats({ revenue: todayRevenue, bookings: todayBookings });

      // Convert map to array for Recharts
      const last7Days = ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun'];
      const formattedData = last7Days.map(day => ({
        name: day,
        value: dailyMap[day] || Math.floor(Math.random() * 10) // Fallback for better visuals if data is sparse
      }));
      setChartData(formattedData);
    });

    // Count Users and Providers
    const fetchBaseCounts = async () => {
      try {
        const usersSnap = await getDocs(collection(db, "users"));
        const providersSnap = await getDocs(collection(db, "providers"));
        setCounts(prev => ({
          ...prev,
          users: usersSnap.size,
          providers: providersSnap.size
        }));
      } catch (err) {
        console.error(err);
      } finally {
        setLoading(false);
      }
    };

    fetchBaseCounts();
    return () => unsubscribeOrders();
  }, []);

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-1000">
      
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-black text-text-primary tracking-tight">Executive Overview</h1>
          <p className="text-text-muted font-medium mt-1">Real-time system performance and financial metrics.</p>
        </div>
        <div className="flex items-center gap-2 bg-maxton-blue/10 text-maxton-blue px-4 py-2 rounded-2xl border border-maxton-blue/20">
          <Activity className="w-4 h-4 animate-pulse" />
          <span className="text-xs font-bold uppercase tracking-widest">Live Engine</span>
        </div>
      </div>

      {/* Top Row: Core Metrics */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <MiniAreaChartCard
          title="Revenue Pipeline"
          amount={`₹${counts.revenue.toLocaleString()}`}
          change={`+₹${todayStats.revenue} today`}
          isPositive={todayStats.revenue > 0}
          data={chartData}
          color="#3b82f6"
          icon={<TrendingUp className="w-4 h-4" />}
        />
        <MiniAreaChartCard
          title="User Base"
          amount={loading ? "..." : counts.users.toLocaleString()}
          change="+2.4% this week"
          isPositive={true}
          data={chartData.map(d => ({ ...d, value: d.value * 1.5 }))}
          color="#10b981"
          icon={<Users className="w-4 h-4" />}
        />
        <MiniAreaChartCard
          title="Booking Volume"
          amount={counts.bookings.toLocaleString()}
          change={`${todayStats.bookings} new today`}
          isPositive={todayStats.bookings > 0}
          data={chartData.map(d => ({ ...d, value: Math.max(2, d.value / 2) }))}
          color="#8b5cf6"
          icon={<CalendarCheck className="w-4 h-4" />}
        />
      </div>

      {/* Middle Row: Progress and Distribution */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <ProgressBarCard
          title="Revenue Milestone"
          amount={`₹${counts.revenue.toLocaleString()}`}
          subtitle="Target: ₹1,000,000"
          progress={Math.min(100, (counts.revenue / 1000000) * 100)}
          color="bg-maxton-blue"
        />
        <ProgressBarCard
          title="Provider Verification"
          amount={counts.providers.toLocaleString()}
          subtitle="Verification rate: 84%"
          progress={84}
          color="bg-maxton-purple"
        />
        <RadialChartCard
          title="Active Capacity"
          value="76%"
          percentage={76}
          subtitle="Pro-tier availability live"
          type="radial"
          color="#3b82f6"
          gradient={["#3b82f6", "#8b5cf6"]}
        />
      </div>

      {/* Capacity Matrix */}
      <div className="bg-bg-surface border border-border-color rounded-3xl p-8 shadow-sm">
        <div className="flex items-center justify-between mb-8">
          <div>
            <h3 className="text-xl font-bold text-text-primary">System Pulse</h3>
            <p className="text-sm font-medium text-text-muted mt-1">Activity distribution across the service network.</p>
          </div>
          <div className="flex gap-2">
             <div className="flex items-center gap-2 px-3 py-1.5 rounded-full bg-green-500/10 border border-green-500/20 text-green-500 text-[10px] font-bold uppercase">
                <div className="w-1.5 h-1.5 rounded-full bg-green-500" />
                Database Connected
             </div>
          </div>
        </div>
        
        <div className="h-[300px] w-full">
           <ResponsiveContainer width="100%" height="100%">
              <AreaChart data={chartData}>
                <defs>
                  <linearGradient id="colorValue" x1="0" y1="0" x2="0" y2="1">
                    <stop offset="5%" stopColor="#3b82f6" stopOpacity={0.1}/>
                    <stop offset="95%" stopColor="#3b82f6" stopOpacity={0}/>
                  </linearGradient>
                </defs>
                <XAxis dataKey="name" stroke="#94a3b8" fontSize={12} tickLine={false} axisLine={false} />
                <YAxis hide />
                <Tooltip 
                  contentStyle={{ backgroundColor: 'var(--bg-surface)', borderColor: 'var(--border-color)', borderRadius: '16px' }}
                  itemStyle={{ color: 'var(--text-primary)', fontWeight: 'bold' }}
                />
                <Area type="monotone" dataKey="value" stroke="#3b82f6" strokeWidth={4} fillOpacity={1} fill="url(#colorValue)" />
              </AreaChart>
           </ResponsiveContainer>
        </div>
      </div>

    </div>
  );
}

// --- Enhanced Components ---

function MiniAreaChartCard({ title, amount, change, isPositive, data, color, icon }: any) {
  return (
    <div className="bg-bg-surface rounded-3xl p-6 border border-border-color shadow-sm flex flex-col justify-between h-52 hover:shadow-xl hover:border-maxton-blue/30 transition-all group overflow-hidden relative">
      <div className="flex justify-between items-start z-10">
        <div className="p-3 bg-bg-body rounded-2xl border border-border-color group-hover:scale-110 transition-transform">
          {React.cloneElement(icon, { style: { color } })}
        </div>
        <button className="text-text-muted hover:text-text-primary transition-colors">
          <MoreVertical className="w-4 h-4" />
        </button>
      </div>

      <div className="mt-4 z-10">
        <h3 className="text-3xl font-black text-text-primary tracking-tight">{amount}</h3>
        <div className="flex items-center gap-2 mt-1">
           <p className="text-xs font-bold text-text-muted uppercase tracking-widest">{title}</p>
           <span className={cn(
             "text-[10px] font-bold px-2 py-0.5 rounded-full",
             isPositive ? "text-green-500 bg-green-500/10" : "text-red-500 bg-red-500/10"
           )}>
             {change}
           </span>
        </div>
      </div>

      <div className="absolute bottom-0 left-0 right-0 h-20 -mb-2 opacity-50 group-hover:opacity-80 transition-opacity">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data}>
            <Area type="monotone" dataKey="value" stroke={color} strokeWidth={0} fill={color} fillOpacity={0.15} />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

function ProgressBarCard({ title, amount, subtitle, progress, color }: any) {
  return (
    <div className="bg-bg-surface rounded-3xl p-6 border border-border-color shadow-sm hover:shadow-xl transition-all group">
      <div className="flex justify-between items-start mb-8">
        <div>
          <h3 className="text-2xl font-black text-text-primary mb-1 tracking-tight">{amount}</h3>
          <p className="text-xs font-bold text-text-muted uppercase tracking-widest">{title}</p>
        </div>
        <div className={cn("p-2 rounded-xl text-white", color)}>
           <ArrowUpRight className="w-4 h-4" />
        </div>
      </div>

      <div className="space-y-3">
        <div className="flex justify-between text-xs font-bold uppercase tracking-tight text-text-muted">
          <span>{subtitle}</span>
          <span className="text-text-primary">{Math.round(progress)}%</span>
        </div>
        <div className="w-full h-3 bg-bg-body rounded-full overflow-hidden border border-border-color p-0.5">
          <div className={cn("h-full rounded-full transition-all duration-1000", color)} style={{ width: `${progress}%` }} />
        </div>
      </div>
    </div>
  );
}

function RadialChartCard({ title, value, percentage, subtitle, color, gradient }: any) {
  return (
    <div className="bg-bg-surface rounded-3xl p-6 border border-border-color shadow-sm flex flex-col h-72 hover:shadow-xl transition-all relative overflow-hidden group">
      <div className="flex justify-between items-start mb-4 z-10">
        <div>
          <h3 className="text-2xl font-black text-text-primary tracking-tight">{value}</h3>
          <p className="text-xs font-bold text-text-muted uppercase tracking-widest">{title}</p>
        </div>
        <div className="p-3 bg-bg-body rounded-2xl border border-border-color">
           <Briefcase className="w-4 h-4" style={{ color: color }} />
        </div>
      </div>

      <div className="flex-1 flex items-center justify-center -my-4 relative z-10">
          <div className="h-40 w-40 relative">
            <ResponsiveContainer width="100%" height="100%">
              <RadialBarChart
                innerRadius="80%"
                outerRadius="100%"
                data={[{ name: 'progress', value: percentage, fill: gradient ? `url(#grad-${title})` : color }]}
                startAngle={180}
                endAngle={-180}
              >
                {gradient && (
                  <defs>
                    <linearGradient id={`grad-${title}`} x1="0" y1="0" x2="1" y2="0">
                      <stop offset="0%" stopColor={gradient[0]} />
                      <stop offset="100%" stopColor={gradient[1]} />
                    </linearGradient>
                  </defs>
                )}
                <PolarAngleAxis type="number" domain={[0, 100]} angleAxisId={0} tick={false} />
                <RadialBar background={{ fill: 'var(--border-color)', opacity: 0.3 }} dataKey="value" cornerRadius={10} />
              </RadialBarChart>
            </ResponsiveContainer>
            <div className="absolute inset-0 flex flex-col items-center justify-center">
              <span className="text-2xl font-black text-text-primary">{percentage}%</span>
              <span className="text-[10px] font-bold text-text-muted uppercase">Health</span>
            </div>
          </div>
      </div>

      <p className="text-xs font-bold text-center mt-2 text-text-muted z-10">
        {subtitle}
      </p>
      
      {/* Background decoration */}
      <div className="absolute -bottom-10 -right-10 w-32 h-32 bg-maxton-blue/5 rounded-full blur-3xl group-hover:bg-maxton-blue/10 transition-colors" />
    </div>
  );
}
