"use client";

import React, { useState, useEffect } from "react";
import { 
  Users, 
  TrendingUp, 
  Wallet,
  ArrowUpRight,
  ArrowDownRight,
  MoreVertical
} from "lucide-react";
import { cn } from "@/lib/utils";
import { 
  AreaChart, Area, XAxis, YAxis, CartesianGrid, Tooltip, ResponsiveContainer,
  RadialBarChart, RadialBar, PolarAngleAxis
} from 'recharts';

// --- Chart Data ---
const salesData = [
  { name: 'Jan', value: 4000 }, { name: 'Feb', value: 3000 },
  { name: 'Mar', value: 5000 }, { name: 'Apr', value: 2780 },
  { name: 'May', value: 6890 }, { name: 'Jun', value: 2390 },
  { name: 'Jul', value: 3490 }, { name: 'Aug', value: 8000 },
];

const accountsData = [
  { name: '1', value: 10 }, { name: '2', value: 40 }, { name: '3', value: 20 },
  { name: '4', value: 50 }, { name: '5', value: 30 }, { name: '6', value: 80 },
];

const weeklySalesData = [
  { name: 'M', value: 20 }, { name: 'T', value: 40 }, { name: 'W', value: 30 },
  { name: 'T', value: 70 }, { name: 'F', value: 40 }, { name: 'S', value: 90 }, { name: 'S', value: 50 }
];

export default function Dashboard() {
  const [isMounted, setIsMounted] = useState(false);

  useEffect(() => {
    setIsMounted(true);
  }, []);

  if (!isMounted) return null; // Prevents hydration mismatch with recharts

  return (
    <div className="space-y-6 animate-in fade-in slide-in-from-bottom-4 duration-1000">
      
      {/* Top Row: Area Charts */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <MiniAreaChartCard 
          title="Total Sales" 
          amount="$9,568" 
          change="-8.6%" 
          isPositive={false}
          data={salesData} 
          color="#10b981" 
        />
        <MiniAreaChartCard 
          title="Total Accounts" 
          amount="85,247" 
          change="+23.7%" 
          isPositive={true}
          data={accountsData} 
          color="#eab308" 
        />
        <MiniAreaChartCard 
          title="Average Weekly Sales" 
          amount="$69,452" 
          change="-8.6%" 
          isPositive={false}
          data={weeklySalesData} 
          color="#06b6d4" 
        />
      </div>

      {/* Middle Row: Progress Bars */}
      <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
        <ProgressBarCard 
          title="Sale This Year" 
          amount="$65,129" 
          change="+24.7%" 
          progress={68} 
          color="bg-maxton-blue" 
        />
        <ProgressBarCard 
          title="Sale This Month" 
          amount="$88,367" 
          change="+18.6%" 
          progress={78} 
          color="bg-maxton-pink" 
        />
        <ProgressBarCard 
          title="Sale This Week" 
          amount="$55,674" 
          change="+42.6%" 
          progress={88} 
          color="bg-maxton-green" 
        />
      </div>

      {/* Bottom Row: Radial Charts */}
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
        <RadialChartCard 
          title="Total Users" 
          value="97.4K" 
          percentage={78} 
          subtitle="12.5% from last month" 
          type="bar"
          color="#22c55e" 
        />
        <RadialChartCard 
          title="Active Users" 
          value="42.5K" 
          percentage={78} 
          subtitle="24K users increased from last month" 
          type="radial"
          color="#eab308" 
          gradient={["#f59e0b", "#ec4899"]}
        />
        <RadialChartCard 
          title="Total Users" 
          value="97.4K" 
          percentage={78} 
          subtitle="12.5% from last month" 
          type="line"
          color="#3b82f6" 
          gradient={["#10b981", "#3b82f6"]}
        />
        <RadialChartCard 
          title="Active Users" 
          value="42.5K" 
          percentage={78} 
          subtitle="24K users increased from last month" 
          type="radial-dots"
          color="#8b5cf6" 
          gradient={["#8b5cf6", "#3b82f6"]}
        />
      </div>

    </div>
  );
}

// --- Component Definitions ---

function MiniAreaChartCard({ title, amount, change, isPositive, data, color }: any) {
  return (
    <div className="bg-bg-surface rounded-2xl p-6 border border-border-color shadow-sm flex flex-col justify-between h-48 hover:shadow-md transition-shadow">
      <div>
        <div className="flex items-center gap-3 mb-1">
          <h3 className="text-2xl font-bold text-text-primary">{amount}</h3>
          <span className={cn(
            "text-xs font-bold px-2 py-0.5 rounded flex items-center gap-1",
            isPositive ? "text-green-500 bg-green-500/10" : "text-red-500 bg-red-500/10"
          )}>
            {isPositive ? <ArrowUpRight className="w-3 h-3" /> : <ArrowDownRight className="w-3 h-3" />}
            {change.replace(/[+-]/, '')}
          </span>
        </div>
        <p className="text-sm font-medium text-text-muted">{title}</p>
      </div>
      
      <div className="h-24 w-full -mx-2">
        <ResponsiveContainer width="100%" height="100%">
          <AreaChart data={data}>
            <defs>
              <linearGradient id={`gradient-${title}`} x1="0" y1="0" x2="0" y2="1">
                <stop offset="5%" stopColor={color} stopOpacity={0.3}/>
                <stop offset="95%" stopColor={color} stopOpacity={0}/>
              </linearGradient>
            </defs>
            <Area 
              type="monotone" 
              dataKey="value" 
              stroke={color} 
              strokeWidth={2}
              fillOpacity={1} 
              fill={`url(#gradient-${title})`} 
            />
          </AreaChart>
        </ResponsiveContainer>
      </div>
    </div>
  );
}

function ProgressBarCard({ title, amount, change, progress, color }: any) {
  return (
    <div className="bg-bg-surface rounded-2xl p-6 border border-border-color shadow-sm hover:shadow-md transition-shadow">
      <div className="flex justify-between items-start mb-6">
        <div>
          <h3 className="text-2xl font-bold text-text-primary mb-1">{amount}</h3>
          <p className="text-sm font-medium text-text-muted">{title}</p>
        </div>
        <span className="text-xs font-bold px-2 py-0.5 rounded flex items-center gap-1 text-green-500 bg-green-500/10">
          <ArrowUpRight className="w-3 h-3" />
          {change.replace(/[+-]/, '')}
        </span>
      </div>
      
      <div className="mt-4">
        <div className="flex justify-between text-xs font-medium text-text-muted mb-2">
          <span>285 left to Goal</span>
          <span>{progress}%</span>
        </div>
        <div className="w-full h-1.5 bg-border-color rounded-full overflow-hidden">
          <div className={cn("h-full rounded-full transition-all duration-1000", color)} style={{ width: `${progress}%` }} />
        </div>
      </div>
    </div>
  );
}

function RadialChartCard({ title, value, percentage, subtitle, type, color, gradient }: any) {
  // Mock data for the mini charts
  const barData = [ { v: 20 }, { v: 40 }, { v: 80 }, { v: 50 }, { v: 90 }, { v: 60 }, { v: 40 }, { v: 30 } ];
  const lineData = [ { v: 20 }, { v: 30 }, { v: 25 }, { v: 60 }, { v: 40 }, { v: 80 }, { v: 50 } ];
  
  return (
    <div className="bg-bg-surface rounded-2xl p-6 border border-border-color shadow-sm flex flex-col h-72 hover:shadow-md transition-shadow relative">
      <div className="flex justify-between items-start mb-4">
        <div>
          <h3 className="text-xl font-bold text-text-primary">{value}</h3>
          <p className="text-sm font-medium text-text-muted">{title}</p>
        </div>
        <button className="text-text-muted hover:text-text-primary transition-colors">
          <MoreVertical className="w-4 h-4" />
        </button>
      </div>

      <div className="flex-1 flex items-center justify-center -my-4 relative">
        {type === 'radial' && (
           <div className="h-32 w-32 relative">
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
                  <RadialBar background={{ fill: 'var(--border-color)' }} dataKey="value" cornerRadius={10} />
                </RadialBarChart>
             </ResponsiveContainer>
             <div className="absolute inset-0 flex items-center justify-center">
               <span className="text-xl font-bold text-text-primary">{percentage}%</span>
             </div>
           </div>
        )}
        
        {type === 'bar' && (
          <div className="w-full h-24 flex items-end justify-center gap-1.5 px-4">
            {barData.map((d, i) => (
              <div key={i} className="w-2 rounded-t-sm" style={{ height: `${d.v}%`, backgroundColor: color, opacity: 0.3 + (i * 0.1) }} />
            ))}
          </div>
        )}

        {type === 'line' && (
          <div className="w-full h-24 px-2">
             <ResponsiveContainer width="100%" height="100%">
                <AreaChart data={lineData}>
                  <defs>
                      <linearGradient id={`gradline-${title}`} x1="0" y1="0" x2="1" y2="0">
                        <stop offset="0%" stopColor={gradient[0]} />
                        <stop offset="100%" stopColor={gradient[1]} />
                      </linearGradient>
                    </defs>
                  <Area type="monotone" dataKey="v" stroke={`url(#gradline-${title})`} strokeWidth={3} fill={`url(#gradline-${title})`} fillOpacity={0.1} />
                </AreaChart>
             </ResponsiveContainer>
          </div>
        )}

        {type === 'radial-dots' && (
           <div className="h-32 w-32 relative">
             <ResponsiveContainer width="100%" height="100%">
                <RadialBarChart 
                  innerRadius="85%" 
                  outerRadius="100%" 
                  data={[{ name: 'progress', value: percentage, fill: gradient ? `url(#grad2-${title})` : color }]} 
                  startAngle={90} 
                  endAngle={-270}
                >
                  {gradient && (
                    <defs>
                      <linearGradient id={`grad2-${title}`} x1="0" y1="0" x2="1" y2="1">
                        <stop offset="0%" stopColor={gradient[0]} />
                        <stop offset="100%" stopColor={gradient[1]} />
                      </linearGradient>
                    </defs>
                  )}
                  <PolarAngleAxis type="number" domain={[0, 100]} angleAxisId={0} tick={false} />
                  <RadialBar background={{ fill: 'var(--border-color)' }} dataKey="value" cornerRadius={10} />
                </RadialBarChart>
             </ResponsiveContainer>
             <div className="absolute inset-0 flex items-center justify-center">
               <span className="text-xl font-bold text-text-primary">{percentage}%</span>
             </div>
           </div>
        )}
      </div>

      <p className={cn(
        "text-xs font-medium text-center mt-2",
        title === "Total Users" ? "text-green-500" : "text-text-muted"
      )}>
        {subtitle}
      </p>
    </div>
  );
}
