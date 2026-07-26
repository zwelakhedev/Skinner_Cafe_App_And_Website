import { useState, type ReactNode } from "react";
import {
  Coffee, Menu, X, Search, ShoppingCart, Bell, User, Star, MapPin,
  Clock, Calendar, ChevronRight, Plus, Minus, Trash2, CreditCard,
  Package, Users, BarChart2, Settings, LogOut, TrendingUp, AlertTriangle,
  CheckCircle, XCircle, FileText, Download, Eye, Edit, ChevronDown,
  Wifi, Battery, Signal, Home, Heart, Gift, Phone, Mail, Instagram,
  Twitter, Facebook, ArrowRight, Filter, Grid, List, Zap, Award,
  DollarSign, ShoppingBag, Activity, PieChart, Layers, BookOpen,
  UserCheck, Truck, ClipboardList, ToggleLeft, ToggleRight
} from "lucide-react";
import {
  LineChart, Line, BarChart, Bar, XAxis, YAxis, CartesianGrid,
  Tooltip, ResponsiveContainer, PieChart as RechartsPie, Pie, Cell, Area, AreaChart
} from "recharts";

// ─── Brand Tokens ────────────────────────────────────────────────────────────
const BROWN = "#4E342E";
const BEIGE = "#D7CCC8";
const ORANGE = "#FF9800";
const GREEN = "#4CAF50";
const RED = "#F44336";
const BG = "#FAFAFA";
const CARD = "#FFFFFF";
const MUTED = "#8D6E63";
const DARK_BROWN = "#3E2723";

// ─── Realistic Data ───────────────────────────────────────────────────────────
const menuItems = [
  { id: 1, name: "Espresso", cat: "Hot Coffee", price: 35, available: true, img: "photo-1510707577719-ae7c14805e3a" },
  { id: 2, name: "Cappuccino", cat: "Hot Coffee", price: 45, available: true, img: "photo-1572442388796-11668a67e53d" },
  { id: 3, name: "Flat White", cat: "Hot Coffee", price: 48, available: true, img: "photo-1461023058943-07fcbe16d735" },
  { id: 4, name: "Americano", cat: "Hot Coffee", price: 40, available: true, img: "photo-1509042239860-f550ce710b93" },
  { id: 5, name: "Cold Brew", cat: "Cold Coffee", price: 58, available: true, img: "photo-1461023058943-07fcbe16d735" },
  { id: 6, name: "Iced Latte", cat: "Cold Coffee", price: 55, available: false, img: "photo-1572442388796-11668a67e53d" },
  { id: 7, name: "Chocolate Croissant", cat: "Pastries", price: 45, available: true, img: "photo-1530648672449-81f6c723e2f1" },
  { id: 8, name: "Avocado Toast", cat: "Light Meals", price: 95, available: true, img: "photo-1603046891744-1f9c1b7c9ef3" },
  { id: 9, name: "Granola Bowl", cat: "Light Meals", price: 85, available: true, img: "photo-1511690743698-d9d85f2fbf38" },
  { id: 10, name: "Rooibos Latte", cat: "Hot Beverages", price: 42, available: true, img: "photo-1526160932832-d5e62e4ae24c" },
];

const events = [
  { id: 1, title: "Jazz Night with Sipho Mabuse", date: "Fri 27 Jun", time: "20:00", price: "R120", spots: 12, img: "photo-1514320291840-2e0a9bf2a9ae" },
  { id: 2, title: "Open Mic Wednesday", date: "Wed 2 Jul", time: "19:00", price: "Free", spots: 30, img: "photo-1501386761578-eac5c94b800a" },
  { id: 3, title: "Coffee Tasting Workshop", date: "Sat 5 Jul", time: "10:00", price: "R280", spots: 5, img: "photo-1495474472287-4d71bcdd2085" },
  { id: 4, title: "Poetry & Prose Night", date: "Thu 10 Jul", time: "18:30", price: "R60", spots: 20, img: "photo-1481627834876-b7833e8f5570" },
];

const orders = [
  { id: "SK-1042", customer: "Lerato Dlamini", items: "Cappuccino x2, Croissant", total: 135, status: "Preparing", time: "8 min ago" },
  { id: "SK-1043", customer: "Thabo Nkosi", items: "Cold Brew, Avocado Toast", total: 153, status: "Received", time: "2 min ago" },
  { id: "SK-1044", customer: "Zanele Mokoena", items: "Espresso x3", total: 105, status: "Ready", time: "12 min ago" },
  { id: "SK-1045", customer: "Sipho Khumalo", items: "Flat White, Granola Bowl", total: 133, status: "Completed", time: "18 min ago" },
  { id: "SK-1046", customer: "Amahle Ntuli", items: "Rooibos Latte x2", total: 84, status: "Preparing", time: "5 min ago" },
];

const staff = [
  { id: 1, name: "Thabo Nkosi", role: "Barista", shift: "07:00–15:00", status: "Active", joined: "Jan 2024" },
  { id: 2, name: "Lerato Dlamini", role: "Cashier", shift: "08:00–16:00", status: "Active", joined: "Mar 2024" },
  { id: 3, name: "Sipho Mokoena", role: "Supervisor", shift: "07:00–15:00", status: "Active", joined: "Nov 2023" },
  { id: 4, name: "Zanele Khumalo", role: "Barista", shift: "14:00–22:00", status: "Off-duty", joined: "Feb 2024" },
  { id: 5, name: "Amahle Cele", role: "Kitchen", shift: "09:00–17:00", status: "Active", joined: "Apr 2024" },
];

const inventory = [
  { id: 1, item: "Coffee Beans (Arabica)", stock: 4.2, unit: "kg", min: 5, supplier: "Cape Coffee Roasters" },
  { id: 2, item: "Whole Milk", stock: 18, unit: "L", min: 10, supplier: "Dairy Gold SA" },
  { id: 3, item: "Oat Milk", stock: 2, unit: "L", min: 4, supplier: "Oatly Africa" },
  { id: 4, item: "Sugar (Cane)", stock: 12, unit: "kg", min: 5, supplier: "Illovo Sugar" },
  { id: 5, item: "Croissants", stock: 8, unit: "units", min: 12, supplier: "Fournos Bakery" },
  { id: 6, item: "Avocados", stock: 15, unit: "units", min: 10, supplier: "Fresh Direct" },
];

const revenueData = [
  { day: "Mon", revenue: 4200, orders: 87 },
  { day: "Tue", revenue: 5100, orders: 104 },
  { day: "Wed", revenue: 6800, orders: 142 },
  { day: "Thu", revenue: 5900, orders: 121 },
  { day: "Fri", revenue: 8400, orders: 178 },
  { day: "Sat", revenue: 9200, orders: 196 },
  { day: "Sun", revenue: 7300, orders: 155 },
];

const topProducts = [
  { name: "Cappuccino", sales: 312, revenue: 14040 },
  { name: "Flat White", sales: 278, revenue: 13344 },
  { name: "Cold Brew", sales: 201, revenue: 11658 },
  { name: "Avocado Toast", sales: 156, revenue: 14820 },
  { name: "Americano", sales: 143, revenue: 5720 },
];

const peakHours = [
  { hour: "7am", visits: 22 }, { hour: "8am", visits: 68 }, { hour: "9am", visits: 94 },
  { hour: "10am", visits: 71 }, { hour: "11am", visits: 55 }, { hour: "12pm", visits: 88 },
  { hour: "1pm", visits: 102 }, { hour: "2pm", visits: 79 }, { hour: "3pm", visits: 61 },
  { hour: "4pm", visits: 48 }, { hour: "5pm", visits: 73 }, { hour: "6pm", visits: 91 },
  { hour: "7pm", visits: 84 }, { hour: "8pm", visits: 67 },
];

const salesCatData = [
  { name: "Hot Coffee", value: 42 },
  { name: "Cold Coffee", value: 21 },
  { name: "Light Meals", value: 18 },
  { name: "Pastries", value: 11 },
  { name: "Other", value: 8 },
];

const PIE_COLORS = [ORANGE, BROWN, GREEN, BEIGE, MUTED];

const suppliers = [
  { name: "Cape Coffee Roasters", contact: "info@capecoffee.co.za", phone: "021 555 0120", items: 3, status: "Active" },
  { name: "Dairy Gold SA", contact: "orders@dairygold.co.za", phone: "011 666 0340", items: 2, status: "Active" },
  { name: "Fournos Bakery", contact: "wholesale@fournos.co.za", phone: "011 442 5050", items: 4, status: "Active" },
  { name: "Fresh Direct", contact: "fresh@direct.co.za", phone: "010 223 4567", items: 6, status: "Inactive" },
];

// ─── Shared UI Components ─────────────────────────────────────────────────────
const Badge = ({ children, color = "orange" }: { children: ReactNode; color?: string }) => {
  const colors: Record<string, string> = {
    orange: `bg-[${ORANGE}]/10 text-[${ORANGE}]`,
    green: "bg-green-100 text-green-700",
    red: "bg-red-100 text-red-600",
    brown: `bg-[${BEIGE}] text-[${BROWN}]`,
    gray: "bg-gray-100 text-gray-600",
    yellow: "bg-yellow-100 text-yellow-700",
  };
  const cls: Record<string, string> = {
    orange: "bg-orange-100 text-orange-600",
    green: "bg-green-100 text-green-700",
    red: "bg-red-100 text-red-600",
    brown: "bg-amber-100 text-amber-900",
    gray: "bg-gray-100 text-gray-600",
    yellow: "bg-yellow-100 text-yellow-700",
  };
  return (
    <span className={`inline-flex items-center px-2.5 py-0.5 rounded-full text-xs font-medium ${cls[color] || cls.gray}`}>
      {children}
    </span>
  );
};

const statusColor = (s: string) => {
  if (s === "Active" || s === "Completed" || s === "Ready") return "green";
  if (s === "Received") return "orange";
  if (s === "Preparing") return "yellow";
  if (s === "Inactive" || s === "Off-duty") return "gray";
  return "gray";
};

const Card = ({ children, className = "" }: { children: ReactNode; className?: string }) => (
  <div className={`bg-white rounded-xl border border-[rgba(78,52,46,0.10)] shadow-sm ${className}`}>{children}</div>
);

const StatCard = ({ icon: Icon, label, value, sub, trend }: any) => (
  <Card className="p-5">
    <div className="flex items-start justify-between">
      <div>
        <p className="text-sm text-[#8D6E63] font-medium">{label}</p>
        <p className="text-2xl font-bold text-[#1A1210] mt-1 font-[Poppins]">{value}</p>
        {sub && <p className="text-xs text-[#8D6E63] mt-1">{sub}</p>}
      </div>
      <div className="w-10 h-10 rounded-xl flex items-center justify-center" style={{ background: `${ORANGE}20` }}>
        <Icon size={20} style={{ color: ORANGE }} />
      </div>
    </div>
    {trend !== undefined && (
      <div className="mt-3 flex items-center gap-1 text-xs">
        <TrendingUp size={12} className="text-green-500" />
        <span className="text-green-600 font-medium">{trend}</span>
        <span className="text-[#8D6E63]">vs last week</span>
      </div>
    )}
  </Card>
);

const Input = ({ label, type = "text", placeholder, icon: Icon }: any) => (
  <div>
    {label && <label className="block text-sm font-medium text-[#1A1210] mb-1.5">{label}</label>}
    <div className="relative">
      {Icon && <Icon size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#8D6E63]" />}
      <input
        type={type}
        placeholder={placeholder}
        className="w-full rounded-lg border border-[rgba(78,52,46,0.15)] bg-[#F3EDE9] px-3 py-2.5 text-sm text-[#1A1210] placeholder-[#8D6E63] focus:outline-none focus:ring-2 focus:ring-[#FF9800]/40 focus:border-[#FF9800]"
        style={{ paddingLeft: Icon ? "2.25rem" : "" }}
      />
    </div>
  </div>
);

const PrimaryBtn = ({ children, className = "", small = false }: any) => (
  <button
    className={`rounded-lg font-semibold text-white transition-all hover:opacity-90 active:scale-95 ${small ? "px-3 py-1.5 text-xs" : "px-5 py-2.5 text-sm"} ${className}`}
    style={{ background: BROWN }}
  >
    {children}
  </button>
);

const AccentBtn = ({ children, className = "", small = false }: any) => (
  <button
    className={`rounded-lg font-semibold text-white transition-all hover:opacity-90 active:scale-95 ${small ? "px-3 py-1.5 text-xs" : "px-5 py-2.5 text-sm"} ${className}`}
    style={{ background: ORANGE }}
  >
    {children}
  </button>
);

const OutlineBtn = ({ children, className = "", small = false }: any) => (
  <button
    className={`rounded-lg font-semibold border transition-all hover:opacity-80 ${small ? "px-3 py-1.5 text-xs" : "px-5 py-2.5 text-sm"} ${className}`}
    style={{ borderColor: BROWN, color: BROWN }}
  >
    {children}
  </button>
);

// ─── SECTION: DESIGN SYSTEM ───────────────────────────────────────────────────
function DesignSystemScreen() {
  return (
    <div className="p-8 space-y-10 max-w-5xl mx-auto">
      <div>
        <h1 className="text-3xl font-bold text-[#1A1210] font-[Poppins]">Skinner Cafe — Design System</h1>
        <p className="text-[#8D6E63] mt-1">Visual language for Braamfontein's most loved café</p>
      </div>

      {/* Logo */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Logo</h2>
        <div className="flex items-center gap-8">
          <div className="flex items-center gap-3 px-6 py-4 rounded-2xl" style={{ background: BROWN }}>
            <Coffee size={28} color={ORANGE} />
            <span className="text-white font-bold text-xl font-[Poppins] tracking-tight">Skinner Cafe</span>
          </div>
          <div className="flex items-center gap-3 px-6 py-4 rounded-2xl border" style={{ borderColor: BROWN }}>
            <Coffee size={28} style={{ color: BROWN }} />
            <span className="font-bold text-xl font-[Poppins] tracking-tight" style={{ color: BROWN }}>Skinner Cafe</span>
          </div>
          <div className="w-14 h-14 rounded-2xl flex items-center justify-center" style={{ background: BROWN }}>
            <Coffee size={28} color={ORANGE} />
          </div>
        </div>
      </section>

      {/* Colors */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Colour Palette</h2>
        <div className="grid grid-cols-6 gap-3">
          {[
            { name: "Coffee Brown", hex: "#4E342E", label: "Primary" },
            { name: "Warm Beige", hex: "#D7CCC8", label: "Secondary" },
            { name: "Warm Orange", hex: "#FF9800", label: "Accent" },
            { name: "Forest Green", hex: "#4CAF50", label: "Success" },
            { name: "Alert Red", hex: "#F44336", label: "Error" },
            { name: "Off-White", hex: "#FAFAFA", label: "Background" },
          ].map(c => (
            <div key={c.hex}>
              <div className="h-16 rounded-xl border border-[rgba(0,0,0,0.08)] mb-2" style={{ background: c.hex }} />
              <p className="text-xs font-semibold text-[#1A1210]">{c.name}</p>
              <p className="text-xs text-[#8D6E63]">{c.hex}</p>
              <p className="text-xs text-[#8D6E63]">{c.label}</p>
            </div>
          ))}
        </div>
      </section>

      {/* Typography */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Typography</h2>
        <Card className="p-6 space-y-4">
          <div><p className="text-4xl font-bold text-[#1A1210] font-[Poppins]">Display Heading — Poppins 700</p></div>
          <div><p className="text-2xl font-semibold text-[#1A1210] font-[Poppins]">Section Title — Poppins 600</p></div>
          <div><p className="text-xl font-medium text-[#1A1210] font-[Poppins]">Card Heading — Poppins 500</p></div>
          <div><p className="text-base text-[#1A1210] font-[Inter]">Body copy for descriptions, table rows and general content. Inter Regular 16px with comfortable leading.</p></div>
          <div><p className="text-sm text-[#8D6E63] font-[Inter]">Supporting text and labels. Inter Regular 14px, muted colour.</p></div>
          <div><p className="text-xs font-semibold text-[#8D6E63] uppercase tracking-widest font-[Inter]">Overline / Section Label</p></div>
        </Card>
      </section>

      {/* Buttons */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Buttons</h2>
        <div className="flex flex-wrap gap-3 items-center">
          <PrimaryBtn>Primary Button</PrimaryBtn>
          <AccentBtn>Accent Button</AccentBtn>
          <OutlineBtn>Outline Button</OutlineBtn>
          <button className="px-5 py-2.5 text-sm font-semibold rounded-lg bg-[#F3EDE9] text-[#4E342E] hover:opacity-80">Ghost Button</button>
          <button className="px-5 py-2.5 text-sm font-semibold rounded-lg bg-red-500 text-white hover:opacity-80">Danger Button</button>
          <PrimaryBtn small>Small Primary</PrimaryBtn>
          <AccentBtn small>Small Accent</AccentBtn>
        </div>
      </section>

      {/* Inputs */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Input Fields</h2>
        <div className="grid grid-cols-3 gap-4">
          <Input label="Email Address" type="email" placeholder="hello@skinnercafe.co.za" icon={Mail} />
          <Input label="Search" placeholder="Search menu..." icon={Search} />
          <div>
            <label className="block text-sm font-medium text-[#1A1210] mb-1.5">Category</label>
            <div className="relative">
              <select className="w-full rounded-lg border border-[rgba(78,52,46,0.15)] bg-[#F3EDE9] px-3 py-2.5 text-sm text-[#1A1210] appearance-none focus:outline-none focus:ring-2 focus:ring-[#FF9800]/40">
                <option>Hot Coffee</option>
                <option>Cold Coffee</option>
                <option>Pastries</option>
              </select>
              <ChevronDown size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-[#8D6E63] pointer-events-none" />
            </div>
          </div>
        </div>
      </section>

      {/* Badges */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Status Badges</h2>
        <div className="flex flex-wrap gap-2">
          <Badge color="green">Active</Badge>
          <Badge color="orange">Pending</Badge>
          <Badge color="yellow">Preparing</Badge>
          <Badge color="red">Inactive</Badge>
          <Badge color="brown">Featured</Badge>
          <Badge color="gray">Off-duty</Badge>
        </div>
      </section>

      {/* Cards */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Cards</h2>
        <div className="grid grid-cols-3 gap-4">
          <Card className="overflow-hidden">
            <img src="https://images.unsplash.com/photo-1572442388796-11668a67e53d?w=400&h=180&fit=crop&auto=format" alt="Cappuccino" className="w-full h-32 object-cover" />
            <div className="p-4">
              <p className="font-semibold text-[#1A1210]">Cappuccino</p>
              <p className="text-sm text-[#8D6E63]">Rich espresso with velvety foam</p>
              <div className="flex items-center justify-between mt-3">
                <span className="font-bold text-[#4E342E]">R45</span>
                <AccentBtn small>Add to Cart</AccentBtn>
              </div>
            </div>
          </Card>
          <StatCard icon={DollarSign} label="Revenue Today" value="R24,580" sub="347 transactions" trend="+18.2%" />
          <Card className="p-4">
            <div className="flex items-start gap-3">
              <div className="w-10 h-10 rounded-full bg-orange-100 flex items-center justify-center flex-shrink-0">
                <AlertTriangle size={18} className="text-orange-500" />
              </div>
              <div>
                <p className="font-semibold text-[#1A1210] text-sm">Low Stock Alert</p>
                <p className="text-xs text-[#8D6E63] mt-0.5">Oat Milk is running low — 2L remaining (min 4L)</p>
                <button className="text-xs font-semibold mt-2" style={{ color: ORANGE }}>Reorder Now →</button>
              </div>
            </div>
          </Card>
        </div>
      </section>

      {/* Table */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Table</h2>
        <Card className="overflow-hidden">
          <table className="w-full text-sm">
            <thead>
              <tr className="border-b border-[rgba(78,52,46,0.08)]" style={{ background: "#F3EDE9" }}>
                <th className="text-left py-3 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">Order</th>
                <th className="text-left py-3 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">Customer</th>
                <th className="text-left py-3 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">Total</th>
                <th className="text-left py-3 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">Status</th>
              </tr>
            </thead>
            <tbody>
              {orders.slice(0, 3).map(o => (
                <tr key={o.id} className="border-b border-[rgba(78,52,46,0.06)] hover:bg-[#FAFAFA]">
                  <td className="py-3 px-4 font-medium text-[#4E342E]">{o.id}</td>
                  <td className="py-3 px-4 text-[#1A1210]">{o.customer}</td>
                  <td className="py-3 px-4 font-semibold text-[#1A1210]">R{o.total}</td>
                  <td className="py-3 px-4"><Badge color={statusColor(o.status)}>{o.status}</Badge></td>
                </tr>
              ))}
            </tbody>
          </table>
        </Card>
      </section>

      {/* Toast */}
      <section>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-4">Toast Notifications</h2>
        <div className="space-y-2 max-w-sm">
          {[
            { msg: "Order SK-1042 is ready for pickup!", color: "bg-green-50 border-green-200", icon: CheckCircle, ic: "text-green-500" },
            { msg: "Oat Milk stock is critically low.", color: "bg-orange-50 border-orange-200", icon: AlertTriangle, ic: "text-orange-500" },
            { msg: "Payment failed — please retry.", color: "bg-red-50 border-red-200", icon: XCircle, ic: "text-red-500" },
          ].map((t, i) => (
            <div key={i} className={`flex items-center gap-3 px-4 py-3 rounded-xl border text-sm ${t.color}`}>
              <t.icon size={16} className={t.ic} />
              <span className="text-[#1A1210]">{t.msg}</span>
              <X size={14} className="text-[#8D6E63] ml-auto cursor-pointer" />
            </div>
          ))}
        </div>
      </section>
    </div>
  );
}

// ─── SECTION: WEBSITE SCREENS ─────────────────────────────────────────────────

function WebNavbar() {
  return (
    <nav className="flex items-center justify-between px-8 py-4 bg-white border-b border-[rgba(78,52,46,0.10)] sticky top-0 z-10">
      <div className="flex items-center gap-2">
        <Coffee size={24} style={{ color: ORANGE }} />
        <span className="font-bold text-lg font-[Poppins]" style={{ color: BROWN }}>Skinner Cafe</span>
      </div>
      <div className="flex items-center gap-6 text-sm font-medium text-[#1A1210]">
        {["Menu", "Events", "About", "Contact"].map(l => (
          <a key={l} className="hover:text-[#FF9800] transition-colors cursor-pointer">{l}</a>
        ))}
      </div>
      <div className="flex items-center gap-2">
        <div className="relative">
          <input placeholder="Search…" className="w-40 rounded-full border border-[rgba(78,52,46,0.15)] bg-[#F3EDE9] px-4 py-1.5 text-sm focus:outline-none focus:ring-2 focus:ring-[#FF9800]/40 pr-8" />
          <Search size={14} className="absolute right-3 top-1/2 -translate-y-1/2 text-[#8D6E63]" />
        </div>
        <button className="p-2 rounded-full hover:bg-[#F3EDE9]"><ShoppingCart size={18} style={{ color: BROWN }} /></button>
        <PrimaryBtn small>Login</PrimaryBtn>
        <AccentBtn small>Register</AccentBtn>
      </div>
    </nav>
  );
}

function HomeScreen() {
  return (
    <div className="bg-[#FAFAFA]">
      <WebNavbar />
      {/* Hero */}
      <div className="relative h-[480px] overflow-hidden">
        <img src="https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=1400&h=600&fit=crop&auto=format" alt="Skinner Cafe interior" className="w-full h-full object-cover" />
        <div className="absolute inset-0" style={{ background: "linear-gradient(to right, rgba(62,39,35,0.88) 40%, rgba(62,39,35,0.15))" }} />
        <div className="absolute inset-0 flex flex-col justify-center px-16">
          <p className="text-sm font-semibold text-[#FF9800] uppercase tracking-widest mb-3">Braamfontein, Johannesburg</p>
          <h1 className="text-5xl font-bold text-white font-[Poppins] leading-tight max-w-xl">Where Coffee<br />Meets Culture</h1>
          <p className="text-[#D7CCC8] mt-4 max-w-md text-lg">Premium espresso, curated events, and a vibrant creative community. Your third place in the heart of Joburg.</p>
          <div className="flex gap-3 mt-8">
            <AccentBtn>Order Now</AccentBtn>
            <OutlineBtn className="border-white text-white hover:bg-white/10">Browse Events</OutlineBtn>
          </div>
        </div>
      </div>

      {/* Featured Products */}
      <div className="px-12 py-12">
        <div className="flex items-center justify-between mb-6">
          <div>
            <p className="text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">Our Menu</p>
            <h2 className="text-2xl font-bold text-[#1A1210] font-[Poppins]">Fan Favourites</h2>
          </div>
          <OutlineBtn small>View Full Menu</OutlineBtn>
        </div>
        <div className="grid grid-cols-4 gap-5">
          {menuItems.slice(0, 4).map(item => (
            <Card key={item.id} className="overflow-hidden hover:shadow-md transition-shadow cursor-pointer">
              <div className="h-40 bg-amber-50 overflow-hidden">
                <img src={`https://images.unsplash.com/photo-${item.img}?w=400&h=200&fit=crop&auto=format`} alt={item.name} className="w-full h-full object-cover" />
              </div>
              <div className="p-4">
                <p className="text-xs text-[#8D6E63] font-medium mb-1">{item.cat}</p>
                <p className="font-semibold text-[#1A1210]">{item.name}</p>
                <div className="flex items-center justify-between mt-3">
                  <span className="font-bold text-[#4E342E]">R{item.price}</span>
                  <button className="w-7 h-7 rounded-full flex items-center justify-center text-white text-sm" style={{ background: ORANGE }}>
                    <Plus size={14} />
                  </button>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Events */}
      <div className="px-12 pb-12" style={{ background: "#F3EDE9" }}>
        <div className="flex items-center justify-between pt-10 mb-6">
          <div>
            <p className="text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">Coming Up</p>
            <h2 className="text-2xl font-bold text-[#1A1210] font-[Poppins]">Upcoming Events</h2>
          </div>
          <OutlineBtn small>See All Events</OutlineBtn>
        </div>
        <div className="grid grid-cols-2 gap-5">
          {events.slice(0, 2).map(e => (
            <Card key={e.id} className="overflow-hidden flex">
              <img src={`https://images.unsplash.com/photo-${e.img}?w=200&h=200&fit=crop&auto=format`} alt={e.title} className="w-32 object-cover flex-shrink-0" />
              <div className="p-5 flex flex-col justify-between">
                <div>
                  <p className="font-semibold text-[#1A1210] font-[Poppins]">{e.title}</p>
                  <div className="flex items-center gap-4 mt-2 text-sm text-[#8D6E63]">
                    <span className="flex items-center gap-1"><Calendar size={13} />{e.date}</span>
                    <span className="flex items-center gap-1"><Clock size={13} />{e.time}</span>
                  </div>
                </div>
                <div className="flex items-center justify-between mt-3">
                  <span className="font-bold text-[#4E342E]">{e.price}</span>
                  <AccentBtn small>RSVP</AccentBtn>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>

      {/* Testimonials */}
      <div className="px-12 py-12">
        <p className="text-xs font-semibold text-[#8D6E63] uppercase tracking-wider mb-2">Reviews</p>
        <h2 className="text-2xl font-bold text-[#1A1210] font-[Poppins] mb-8">What People Say</h2>
        <div className="grid grid-cols-3 gap-5">
          {[
            { name: "Ntombi M.", review: "Best cappuccino in Joburg, hands down. The vibe is unmatched — perfect for deep work sessions or catching up with friends.", stars: 5 },
            { name: "Kagiso T.", review: "I've discovered so many amazing artists at Skinner's events. The poetry nights are genuinely moving. A cultural institution.", stars: 5 },
            { name: "Priya N.", review: "The loyalty programme is a game-changer. I've redeemed enough points for a free coffee every week. Love this place!", stars: 4 },
          ].map((t, i) => (
            <Card key={i} className="p-5">
              <div className="flex gap-0.5 mb-3">
                {Array.from({ length: t.stars }).map((_, j) => <Star key={j} size={14} fill={ORANGE} color={ORANGE} />)}
              </div>
              <p className="text-sm text-[#1A1210] leading-relaxed">"{t.review}"</p>
              <p className="text-sm font-semibold text-[#4E342E] mt-4">{t.name}</p>
            </Card>
          ))}
        </div>
      </div>

      {/* Footer */}
      <footer style={{ background: DARK_BROWN }} className="px-12 py-10 text-white">
        <div className="grid grid-cols-4 gap-8 mb-8">
          <div>
            <div className="flex items-center gap-2 mb-3">
              <Coffee size={20} style={{ color: ORANGE }} />
              <span className="font-bold font-[Poppins]">Skinner Cafe</span>
            </div>
            <p className="text-sm text-[#D7CCC8]">Premium coffee & events in the heart of Braamfontein, Johannesburg.</p>
            <div className="flex gap-3 mt-4">
              {[Instagram, Twitter, Facebook].map((Icon, i) => (
                <div key={i} className="w-8 h-8 rounded-full bg-white/10 flex items-center justify-center cursor-pointer hover:bg-white/20">
                  <Icon size={14} color="white" />
                </div>
              ))}
            </div>
          </div>
          <div>
            <p className="font-semibold mb-3 text-sm uppercase tracking-wider text-[#D7CCC8]">Quick Links</p>
            {["Menu", "Events", "Loyalty", "About Us", "Contact"].map(l => <p key={l} className="text-sm text-[#D7CCC8] py-0.5 hover:text-white cursor-pointer">{l}</p>)}
          </div>
          <div>
            <p className="font-semibold mb-3 text-sm uppercase tracking-wider text-[#D7CCC8]">Hours</p>
            <div className="text-sm text-[#D7CCC8] space-y-1">
              <p>Mon – Thu: 07:00 – 21:00</p>
              <p>Fri: 07:00 – 23:00</p>
              <p>Sat – Sun: 08:00 – 23:00</p>
            </div>
          </div>
          <div>
            <p className="font-semibold mb-3 text-sm uppercase tracking-wider text-[#D7CCC8]">Find Us</p>
            <p className="text-sm text-[#D7CCC8] flex items-start gap-2"><MapPin size={14} className="mt-0.5 flex-shrink-0" />12 Jorissen Street, Braamfontein, Johannesburg, 2001</p>
            <p className="text-sm text-[#D7CCC8] flex items-center gap-2 mt-2"><Phone size={14} />011 339 5500</p>
            <p className="text-sm text-[#D7CCC8] flex items-center gap-2 mt-1"><Mail size={14} />hello@skinnercafe.co.za</p>
          </div>
        </div>
        <div className="border-t border-white/10 pt-6 text-xs text-[#D7CCC8] flex justify-between">
          <span>© 2025 Skinner Cafe. All rights reserved.</span>
          <span>Made with ♥ in Johannesburg</span>
        </div>
      </footer>
    </div>
  );
}

function MenuScreen() {
  const [active, setActive] = useState("All");
  const cats = ["All", "Hot Coffee", "Cold Coffee", "Pastries", "Light Meals", "Hot Beverages"];
  const filtered = active === "All" ? menuItems : menuItems.filter(i => i.cat === active);
  return (
    <div className="bg-[#FAFAFA]">
      <WebNavbar />
      <div className="px-12 py-8">
        <h1 className="text-3xl font-bold text-[#1A1210] font-[Poppins]">Our Menu</h1>
        <p className="text-[#8D6E63] mt-1">Crafted with intention. Served with love.</p>
        <div className="flex items-center gap-3 mt-6 mb-6">
          <div className="relative flex-1 max-w-sm">
            <Search size={16} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#8D6E63]" />
            <input placeholder="Search menu…" className="w-full pl-10 pr-4 py-2.5 rounded-full border border-[rgba(78,52,46,0.15)] bg-white text-sm focus:outline-none focus:ring-2 focus:ring-[#FF9800]/40" />
          </div>
          <div className="flex gap-2">
            {cats.map(c => (
              <button key={c} onClick={() => setActive(c)} className={`px-4 py-2 rounded-full text-sm font-medium transition-all ${active === c ? "text-white" : "bg-white border border-[rgba(78,52,46,0.15)] text-[#8D6E63] hover:border-[#4E342E]"}`} style={active === c ? { background: BROWN } : {}}>
                {c}
              </button>
            ))}
          </div>
        </div>
        <div className="grid grid-cols-4 gap-5">
          {filtered.map(item => (
            <Card key={item.id} className="overflow-hidden hover:shadow-md transition-shadow">
              <div className="h-44 bg-amber-50 overflow-hidden relative">
                <img src={`https://images.unsplash.com/photo-${item.img}?w=400&h=220&fit=crop&auto=format`} alt={item.name} className="w-full h-full object-cover" />
                {!item.available && (
                  <div className="absolute inset-0 bg-black/50 flex items-center justify-center">
                    <span className="text-white text-xs font-semibold bg-black/60 px-3 py-1 rounded-full">Unavailable</span>
                  </div>
                )}
              </div>
              <div className="p-4">
                <p className="text-xs text-[#8D6E63] font-medium">{item.cat}</p>
                <p className="font-semibold text-[#1A1210] mt-0.5">{item.name}</p>
                <div className="flex items-center justify-between mt-3">
                  <span className="font-bold text-[#4E342E] text-lg">R{item.price}</span>
                  {item.available ? (
                    <AccentBtn small>Add to Cart</AccentBtn>
                  ) : (
                    <span className="text-xs text-[#8D6E63]">Sold out</span>
                  )}
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}

function CartScreen() {
  const cartItems = [
    { ...menuItems[1], qty: 2 },
    { ...menuItems[7], qty: 1 },
    { ...menuItems[3], qty: 1 },
  ];
  const subtotal = cartItems.reduce((a, i) => a + i.price * i.qty, 0);
  return (
    <div className="bg-[#FAFAFA]">
      <WebNavbar />
      <div className="px-12 py-8 max-w-4xl mx-auto">
        <h1 className="text-2xl font-bold text-[#1A1210] font-[Poppins] mb-6">Your Cart</h1>
        <div className="grid grid-cols-3 gap-8">
          <div className="col-span-2 space-y-3">
            {cartItems.map((item, i) => (
              <Card key={i} className="p-4 flex items-center gap-4">
                <img src={`https://images.unsplash.com/photo-${item.img}?w=80&h=80&fit=crop&auto=format`} alt={item.name} className="w-16 h-16 object-cover rounded-lg" />
                <div className="flex-1">
                  <p className="font-semibold text-[#1A1210]">{item.name}</p>
                  <p className="text-sm text-[#8D6E63]">{item.cat}</p>
                </div>
                <div className="flex items-center gap-2">
                  <button className="w-7 h-7 rounded-full border border-[rgba(78,52,46,0.2)] flex items-center justify-center text-[#4E342E] hover:bg-[#F3EDE9]"><Minus size={12} /></button>
                  <span className="w-6 text-center font-semibold text-[#1A1210]">{item.qty}</span>
                  <button className="w-7 h-7 rounded-full border border-[rgba(78,52,46,0.2)] flex items-center justify-center text-[#4E342E] hover:bg-[#F3EDE9]"><Plus size={12} /></button>
                </div>
                <p className="font-bold text-[#4E342E] w-16 text-right">R{item.price * item.qty}</p>
                <button className="text-red-400 hover:text-red-600 ml-2"><Trash2 size={16} /></button>
              </Card>
            ))}
            <Card className="p-4">
              <label className="block text-sm font-medium text-[#1A1210] mb-2">Special Instructions</label>
              <textarea rows={3} placeholder="Any allergies or special requests?" className="w-full rounded-lg border border-[rgba(78,52,46,0.15)] bg-[#F3EDE9] px-3 py-2 text-sm focus:outline-none focus:ring-2 focus:ring-[#FF9800]/40 resize-none" />
            </Card>
          </div>
          <div>
            <Card className="p-5 space-y-3">
              <h3 className="font-semibold text-[#1A1210] font-[Poppins]">Order Summary</h3>
              {cartItems.map((item, i) => (
                <div key={i} className="flex justify-between text-sm text-[#8D6E63]">
                  <span>{item.name} ×{item.qty}</span>
                  <span>R{item.price * item.qty}</span>
                </div>
              ))}
              <div className="border-t border-[rgba(78,52,46,0.1)] pt-3">
                <div className="flex justify-between text-sm"><span className="text-[#8D6E63]">Subtotal</span><span className="font-medium">R{subtotal}</span></div>
                <div className="flex justify-between text-sm mt-1"><span className="text-[#8D6E63]">Service fee</span><span className="font-medium">R10</span></div>
              </div>
              <div className="border-t border-[rgba(78,52,46,0.1)] pt-3 flex justify-between font-bold text-[#1A1210]">
                <span>Total</span><span>R{subtotal + 10}</span>
              </div>
              <AccentBtn className="w-full text-center block">Proceed to Checkout</AccentBtn>
              <p className="text-xs text-[#8D6E63] text-center">Pick-up at Jorissen Street counter</p>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}

function LoginScreen() {
  return (
    <div className="min-h-screen flex" style={{ background: "#FAFAFA" }}>
      <div className="w-1/2 relative hidden lg:block">
        <img src="https://images.unsplash.com/photo-1509042239860-f550ce710b93?w=800&h=900&fit=crop&auto=format" alt="Coffee bar" className="w-full h-full object-cover" />
        <div className="absolute inset-0 flex flex-col justify-end p-12" style={{ background: "linear-gradient(to top, rgba(62,39,35,0.9) 30%, transparent)" }}>
          <div className="flex items-center gap-2 mb-6">
            <Coffee size={28} style={{ color: ORANGE }} />
            <span className="text-white font-bold text-2xl font-[Poppins]">Skinner Cafe</span>
          </div>
          <p className="text-white text-xl font-[Poppins] font-medium">"The best coffee in Braamfontein, paired with the best community in Joburg."</p>
          <p className="text-[#D7CCC8] mt-2">— Ntombi M., Regular Customer</p>
        </div>
      </div>
      <div className="flex-1 flex items-center justify-center p-12">
        <div className="w-full max-w-sm">
          <div className="mb-8">
            <h1 className="text-2xl font-bold text-[#1A1210] font-[Poppins]">Welcome back</h1>
            <p className="text-[#8D6E63] mt-1">Sign in to your Skinner Cafe account</p>
          </div>
          <div className="space-y-4">
            <Input label="Email Address" type="email" placeholder="hello@example.co.za" icon={Mail} />
            <Input label="Password" type="password" placeholder="••••••••" />
            <div className="flex justify-between items-center">
              <label className="flex items-center gap-2 text-sm text-[#8D6E63]">
                <input type="checkbox" className="rounded" /> Remember me
              </label>
              <button className="text-sm font-medium" style={{ color: ORANGE }}>Forgot password?</button>
            </div>
            <PrimaryBtn className="w-full text-center">Sign In</PrimaryBtn>
            <p className="text-sm text-center text-[#8D6E63]">
              Don't have an account? <span className="font-semibold cursor-pointer" style={{ color: BROWN }}>Register</span>
            </p>
          </div>
        </div>
      </div>
    </div>
  );
}

function RegisterScreen() {
  return (
    <div className="min-h-screen flex items-center justify-center bg-[#FAFAFA] py-8">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-4">
            <Coffee size={28} style={{ color: ORANGE }} />
            <span className="font-bold text-xl font-[Poppins]" style={{ color: BROWN }}>Skinner Cafe</span>
          </div>
          <h1 className="text-2xl font-bold text-[#1A1210] font-[Poppins]">Create an account</h1>
          <p className="text-[#8D6E63] mt-1">Join the Skinner community and earn loyalty points</p>
        </div>
        <Card className="p-6">
          <div className="space-y-4">
            <div className="grid grid-cols-2 gap-3">
              <Input label="First Name" placeholder="Thabo" />
              <Input label="Last Name" placeholder="Nkosi" />
            </div>
            <Input label="Phone Number" type="tel" placeholder="072 123 4567" icon={Phone} />
            <Input label="Email Address" type="email" placeholder="thabo@example.co.za" icon={Mail} />
            <Input label="Password" type="password" placeholder="Create a strong password" />
            <Input label="Confirm Password" type="password" placeholder="Repeat password" />
            <label className="flex items-start gap-2 text-sm text-[#8D6E63]">
              <input type="checkbox" className="rounded mt-0.5" />
              I agree to the <span className="font-medium underline cursor-pointer" style={{ color: BROWN }}>Terms of Service</span> and <span className="font-medium underline cursor-pointer" style={{ color: BROWN }}>Privacy Policy</span>
            </label>
            <AccentBtn className="w-full text-center">Create Account</AccentBtn>
            <p className="text-sm text-center text-[#8D6E63]">
              Already have an account? <span className="font-semibold cursor-pointer" style={{ color: BROWN }}>Sign In</span>
            </p>
          </div>
        </Card>
      </div>
    </div>
  );
}

function CustomerDashboard() {
  return (
    <div className="bg-[#FAFAFA]">
      <WebNavbar />
      <div className="px-12 py-8 max-w-5xl mx-auto">
        <div className="flex items-center gap-4 mb-8">
          <div className="w-16 h-16 rounded-full bg-amber-100 flex items-center justify-center">
            <User size={28} style={{ color: BROWN }} />
          </div>
          <div>
            <h1 className="text-2xl font-bold text-[#1A1210] font-[Poppins]">Lerato Dlamini</h1>
            <p className="text-[#8D6E63]">lerato@example.co.za · Member since Jan 2024</p>
          </div>
          <div className="ml-auto">
            <Badge color="orange">Gold Member</Badge>
          </div>
        </div>
        <div className="grid grid-cols-4 gap-4 mb-8">
          <StatCard icon={ShoppingBag} label="Orders" value="47" sub="Lifetime" />
          <StatCard icon={Gift} label="Loyalty Points" value="1,240 pts" sub="≈ R62 value" />
          <StatCard icon={DollarSign} label="Total Spent" value="R4,820" sub="This year" />
          <StatCard icon={Calendar} label="Events RSVP'd" value="6" sub="Upcoming: 2" />
        </div>
        <div className="grid grid-cols-3 gap-6">
          <div className="col-span-2">
            <h2 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Recent Orders</h2>
            <Card className="overflow-hidden">
              <table className="w-full text-sm">
                <thead>
                  <tr className="border-b" style={{ background: "#F3EDE9" }}>
                    <th className="text-left py-2 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide">Order</th>
                    <th className="text-left py-2 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide">Items</th>
                    <th className="text-left py-2 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide">Date</th>
                    <th className="text-left py-2 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide">Total</th>
                  </tr>
                </thead>
                <tbody>
                  {[
                    { id: "SK-1039", items: "Cappuccino, Avocado Toast", date: "23 Jun", total: 140 },
                    { id: "SK-1028", items: "Cold Brew x2", date: "20 Jun", total: 116 },
                    { id: "SK-1015", items: "Flat White, Croissant", date: "17 Jun", total: 93 },
                    { id: "SK-1002", items: "Espresso x3", date: "14 Jun", total: 105 },
                  ].map(o => (
                    <tr key={o.id} className="border-b border-[rgba(78,52,46,0.06)] hover:bg-[#FAFAFA]">
                      <td className="py-3 px-4 font-medium text-[#4E342E]">{o.id}</td>
                      <td className="py-3 px-4 text-[#1A1210]">{o.items}</td>
                      <td className="py-3 px-4 text-[#8D6E63]">{o.date}</td>
                      <td className="py-3 px-4 font-semibold">R{o.total}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </Card>
          </div>
          <div className="space-y-4">
            <h2 className="font-semibold text-[#1A1210] font-[Poppins]">Upcoming Events</h2>
            {events.slice(0, 2).map(e => (
              <Card key={e.id} className="p-4">
                <p className="font-medium text-sm text-[#1A1210]">{e.title}</p>
                <p className="text-xs text-[#8D6E63] mt-1 flex items-center gap-1"><Calendar size={11} />{e.date} at {e.time}</p>
                <p className="text-xs font-semibold mt-2" style={{ color: ORANGE }}>{e.price}</p>
              </Card>
            ))}
            <Card className="p-4" style={{ background: "#F3EDE9" }}>
              <p className="font-semibold text-[#4E342E] text-sm">Loyalty Status</p>
              <div className="mt-2 h-2 rounded-full bg-white overflow-hidden">
                <div className="h-full rounded-full" style={{ width: "62%", background: ORANGE }} />
              </div>
              <p className="text-xs text-[#8D6E63] mt-1">1,240 / 2,000 pts to Platinum</p>
            </Card>
          </div>
        </div>
      </div>
    </div>
  );
}

function EventsScreen() {
  return (
    <div className="bg-[#FAFAFA]">
      <WebNavbar />
      <div className="px-12 py-8">
        <h1 className="text-3xl font-bold text-[#1A1210] font-[Poppins]">Events</h1>
        <p className="text-[#8D6E63] mt-1">Live music, poetry, workshops and more — always something happening at Skinner.</p>
        <div className="grid grid-cols-2 gap-6 mt-8">
          {events.map(e => (
            <Card key={e.id} className="overflow-hidden hover:shadow-md transition-shadow">
              <div className="h-52 overflow-hidden relative">
                <img src={`https://images.unsplash.com/photo-${e.img}?w=700&h=280&fit=crop&auto=format`} alt={e.title} className="w-full h-full object-cover" />
                <div className="absolute top-3 left-3">
                  <span className="text-xs font-bold text-white px-3 py-1 rounded-full" style={{ background: ORANGE }}>{e.price}</span>
                </div>
              </div>
              <div className="p-5">
                <h3 className="font-bold text-[#1A1210] text-lg font-[Poppins]">{e.title}</h3>
                <div className="flex items-center gap-4 mt-2 text-sm text-[#8D6E63]">
                  <span className="flex items-center gap-1"><Calendar size={13} />{e.date}</span>
                  <span className="flex items-center gap-1"><Clock size={13} />{e.time}</span>
                  <span className="flex items-center gap-1"><Users size={13} />{e.spots} spots left</span>
                </div>
                <div className="flex items-center justify-between mt-4">
                  <OutlineBtn small>View Details</OutlineBtn>
                  <AccentBtn small>RSVP Now</AccentBtn>
                </div>
              </div>
            </Card>
          ))}
        </div>
      </div>
    </div>
  );
}

// ─── SECTION: STAFF PORTAL ────────────────────────────────────────────────────
function StaffSidebar({ active, setActive }: any) {
  const links = [
    { id: "ops", label: "Dashboard", icon: Home },
    { id: "pos", label: "Point of Sale", icon: ShoppingBag },
    { id: "orders", label: "Order Queue", icon: ClipboardList },
    { id: "inventory", label: "Inventory", icon: Package },
    { id: "attendance", label: "Attendance", icon: UserCheck },
    { id: "schedule", label: "Schedule", icon: Calendar },
  ];
  return (
    <aside className="w-60 flex-shrink-0 flex flex-col" style={{ background: DARK_BROWN, minHeight: "100vh" }}>
      <div className="px-6 py-5 border-b border-white/10">
        <div className="flex items-center gap-2">
          <Coffee size={22} style={{ color: ORANGE }} />
          <div>
            <p className="text-white font-bold text-sm font-[Poppins]">Skinner Cafe</p>
            <p className="text-[#D7CCC8] text-xs">Staff Portal</p>
          </div>
        </div>
      </div>
      <nav className="flex-1 px-3 py-4">
        {links.map(l => (
          <button key={l.id} onClick={() => setActive(l.id)} className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg mb-1 text-sm font-medium transition-all text-left ${active === l.id ? "text-white" : "text-[#D7CCC8] hover:bg-white/10"}`} style={active === l.id ? { background: BROWN } : {}}>
            <l.icon size={16} />
            {l.label}
          </button>
        ))}
      </nav>
      <div className="px-3 pb-4 border-t border-white/10 pt-4">
        <div className="flex items-center gap-3 px-3 py-2">
          <div className="w-8 h-8 rounded-full bg-orange-400/20 flex items-center justify-center">
            <User size={14} style={{ color: ORANGE }} />
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-white text-xs font-semibold truncate">Sipho Mokoena</p>
            <p className="text-[#D7CCC8] text-xs">Supervisor</p>
          </div>
          <LogOut size={14} className="text-[#D7CCC8]" />
        </div>
      </div>
    </aside>
  );
}

function OpsTopbar() {
  return (
    <div className="flex items-center justify-between px-6 py-3 bg-white border-b border-[rgba(78,52,46,0.10)]">
      <div>
        <p className="text-sm font-semibold text-[#1A1210]">Tuesday, 24 June 2025</p>
        <p className="text-xs text-[#8D6E63]">Shift: 07:00 – 15:00</p>
      </div>
      <div className="flex items-center gap-3">
        <button className="relative p-2 rounded-full hover:bg-[#F3EDE9]">
          <Bell size={18} style={{ color: BROWN }} />
          <span className="absolute top-1 right-1 w-2 h-2 rounded-full" style={{ background: RED }} />
        </button>
        <div className="flex items-center gap-2 px-3 py-1.5 rounded-full" style={{ background: "#F3EDE9" }}>
          <div className="w-2 h-2 rounded-full bg-green-500" />
          <span className="text-xs font-medium" style={{ color: BROWN }}>Open</span>
        </div>
      </div>
    </div>
  );
}

function OpsDashboardContent() {
  return (
    <div className="p-6 space-y-6">
      <h1 className="text-xl font-bold text-[#1A1210] font-[Poppins]">Operational Dashboard</h1>
      <div className="grid grid-cols-5 gap-4">
        <StatCard icon={ShoppingBag} label="Orders Today" value="142" trend="+12%" />
        <StatCard icon={DollarSign} label="Sales Today" value="R8,420" trend="+8%" />
        <StatCard icon={AlertTriangle} label="Low Stock" value="3 items" />
        <StatCard icon={Users} label="Active Staff" value="4" sub="2 on break" />
        <StatCard icon={Calendar} label="Events Today" value="1" sub="Jazz Night 20:00" />
      </div>

      <div className="grid grid-cols-2 gap-6">
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Recent Orders</h3>
          <div className="space-y-2">
            {orders.map(o => (
              <div key={o.id} className="flex items-center justify-between py-2 border-b border-[rgba(78,52,46,0.06)] last:border-0">
                <div>
                  <p className="text-sm font-semibold text-[#1A1210]">{o.id}</p>
                  <p className="text-xs text-[#8D6E63]">{o.items}</p>
                </div>
                <div className="text-right">
                  <Badge color={statusColor(o.status)}>{o.status}</Badge>
                  <p className="text-xs text-[#8D6E63] mt-1">{o.time}</p>
                </div>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Stock Alerts</h3>
          <div className="space-y-3">
            {inventory.filter(i => i.stock < i.min).map(item => (
              <div key={item.id} className="flex items-start gap-3 p-3 rounded-lg bg-red-50 border border-red-100">
                <AlertTriangle size={16} className="text-red-500 mt-0.5 flex-shrink-0" />
                <div className="flex-1">
                  <p className="text-sm font-semibold text-[#1A1210]">{item.item}</p>
                  <p className="text-xs text-[#8D6E63]">{item.stock}{item.unit} remaining (min {item.min}{item.unit})</p>
                </div>
                <AccentBtn small>Order</AccentBtn>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}

function POSScreen() {
  const [order, setOrder] = useState<Array<{item: typeof menuItems[0]; qty: number}>>([]);
  const addItem = (item: typeof menuItems[0]) => {
    setOrder(prev => {
      const ex = prev.find(o => o.item.id === item.id);
      if (ex) return prev.map(o => o.item.id === item.id ? { ...o, qty: o.qty + 1 } : o);
      return [...prev, { item, qty: 1 }];
    });
  };
  const total = order.reduce((a, o) => a + o.item.price * o.qty, 0);
  return (
    <div className="p-6 flex gap-6 h-full">
      <div className="flex-1">
        <div className="flex items-center justify-between mb-4">
          <h2 className="font-bold text-[#1A1210] font-[Poppins]">Product Catalogue</h2>
          <div className="relative w-60">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#8D6E63]" />
            <input placeholder="Search products…" className="w-full pl-9 pr-3 py-2 rounded-lg border border-[rgba(78,52,46,0.15)] bg-[#F3EDE9] text-sm focus:outline-none" />
          </div>
        </div>
        <div className="grid grid-cols-3 gap-3">
          {menuItems.map(item => (
            <button key={item.id} onClick={() => item.available && addItem(item)} className={`bg-white rounded-xl p-3 border border-[rgba(78,52,46,0.10)] text-left hover:shadow-md transition-all hover:border-[#FF9800] ${!item.available ? "opacity-50 cursor-not-allowed" : ""}`}>
              <img src={`https://images.unsplash.com/photo-${item.img}?w=200&h=120&fit=crop&auto=format`} alt={item.name} className="w-full h-24 object-cover rounded-lg mb-2" />
              <p className="text-xs text-[#8D6E63]">{item.cat}</p>
              <p className="text-sm font-semibold text-[#1A1210]">{item.name}</p>
              <p className="font-bold text-[#4E342E] mt-1">R{item.price}</p>
            </button>
          ))}
        </div>
      </div>
      <div className="w-72 flex-shrink-0">
        <Card className="flex flex-col h-full p-4">
          <h3 className="font-bold text-[#1A1210] font-[Poppins] mb-4">Current Order</h3>
          <div className="flex-1 space-y-2 overflow-y-auto">
            {order.length === 0 ? (
              <p className="text-sm text-[#8D6E63] text-center py-8">Tap a product to add it to the order</p>
            ) : order.map((o, i) => (
              <div key={i} className="flex items-center justify-between text-sm border-b border-[rgba(78,52,46,0.06)] pb-2">
                <div>
                  <p className="font-medium text-[#1A1210]">{o.item.name}</p>
                  <p className="text-xs text-[#8D6E63]">R{o.item.price} each</p>
                </div>
                <div className="flex items-center gap-1">
                  <button onClick={() => setOrder(p => p.map(x => x.item.id === o.item.id ? { ...x, qty: Math.max(0, x.qty - 1) } : x).filter(x => x.qty > 0))} className="w-5 h-5 rounded-full bg-[#F3EDE9] flex items-center justify-center"><Minus size={10} /></button>
                  <span className="w-5 text-center font-semibold">{o.qty}</span>
                  <button onClick={() => addItem(o.item)} className="w-5 h-5 rounded-full bg-[#F3EDE9] flex items-center justify-center"><Plus size={10} /></button>
                </div>
                <span className="font-bold text-[#4E342E] w-12 text-right">R{o.item.price * o.qty}</span>
              </div>
            ))}
          </div>
          <div className="border-t border-[rgba(78,52,46,0.1)] pt-3 mt-3">
            <div className="flex justify-between text-sm mb-1"><span className="text-[#8D6E63]">Subtotal</span><span>R{total}</span></div>
            <div className="flex justify-between font-bold text-lg text-[#1A1210] mb-4"><span>Total</span><span>R{total}</span></div>
            <div className="grid grid-cols-2 gap-2 mb-2">
              <button className="py-2 rounded-lg text-sm font-semibold border text-[#4E342E] border-[rgba(78,52,46,0.2)] flex items-center justify-center gap-1"><CreditCard size={14} />Card</button>
              <button className="py-2 rounded-lg text-sm font-semibold border text-[#4E342E] border-[rgba(78,52,46,0.2)] flex items-center justify-center gap-1"><DollarSign size={14} />Cash</button>
            </div>
            <AccentBtn className="w-full text-center">Process Payment</AccentBtn>
            <button onClick={() => setOrder([])} className="w-full text-center text-sm text-red-400 mt-2 hover:text-red-600">Clear Order</button>
          </div>
        </Card>
      </div>
    </div>
  );
}

function OrderQueueScreen() {
  const cols = ["Received", "Preparing", "Ready", "Completed"];
  const colOrders = (status: string) => orders.filter(o => o.status === status);
  return (
    <div className="p-6">
      <h2 className="font-bold text-[#1A1210] font-[Poppins] mb-6">Order Queue</h2>
      <div className="grid grid-cols-4 gap-4">
        {cols.map(col => (
          <div key={col}>
            <div className="flex items-center justify-between mb-3">
              <h3 className="text-sm font-semibold text-[#1A1210]">{col}</h3>
              <span className="text-xs bg-[#F3EDE9] text-[#8D6E63] px-2 py-0.5 rounded-full">{colOrders(col).length}</span>
            </div>
            <div className="space-y-3">
              {colOrders(col).map(o => (
                <Card key={o.id} className="p-3">
                  <div className="flex items-center justify-between mb-2">
                    <span className="text-sm font-bold text-[#4E342E]">{o.id}</span>
                    <Badge color={statusColor(o.status)}>{o.status}</Badge>
                  </div>
                  <p className="text-xs font-medium text-[#1A1210]">{o.customer}</p>
                  <p className="text-xs text-[#8D6E63] mt-1">{o.items}</p>
                  <p className="text-xs text-[#8D6E63] mt-1">{o.time}</p>
                  <div className="flex gap-2 mt-2">
                    {col !== "Completed" && <AccentBtn small className="flex-1 text-center text-xs py-1">{col === "Received" ? "Start" : col === "Preparing" ? "Ready" : "Done"}</AccentBtn>}
                  </div>
                </Card>
              ))}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function InventoryScreen() {
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-bold text-[#1A1210] font-[Poppins]">Inventory Management</h2>
        <AccentBtn><Plus size={14} className="inline mr-1" />Add Stock</AccentBtn>
      </div>
      <Card className="overflow-hidden">
        <div className="p-4 border-b border-[rgba(78,52,46,0.08)] flex items-center gap-3">
          <div className="relative flex-1 max-w-xs">
            <Search size={14} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#8D6E63]" />
            <input placeholder="Search inventory…" className="w-full pl-9 pr-3 py-2 rounded-lg bg-[#F3EDE9] border-0 text-sm focus:outline-none focus:ring-2 focus:ring-[#FF9800]/40" />
          </div>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-[rgba(78,52,46,0.08)]" style={{ background: "#F3EDE9" }}>
              {["Item", "In Stock", "Min. Level", "Status", "Supplier", "Actions"].map(h => (
                <th key={h} className="text-left py-3 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {inventory.map(item => {
              const low = item.stock < item.min;
              return (
                <tr key={item.id} className={`border-b border-[rgba(78,52,46,0.06)] hover:bg-[#FAFAFA] ${low ? "bg-red-50/50" : ""}`}>
                  <td className="py-3 px-4 font-medium text-[#1A1210]">{item.item}</td>
                  <td className="py-3 px-4">
                    <span className={`font-semibold ${low ? "text-red-600" : "text-green-600"}`}>{item.stock} {item.unit}</span>
                  </td>
                  <td className="py-3 px-4 text-[#8D6E63]">{item.min} {item.unit}</td>
                  <td className="py-3 px-4">
                    {low ? (
                      <span className="flex items-center gap-1 text-xs font-semibold text-red-600"><AlertTriangle size={12} />Low Stock</span>
                    ) : (
                      <span className="flex items-center gap-1 text-xs font-semibold text-green-600"><CheckCircle size={12} />OK</span>
                    )}
                  </td>
                  <td className="py-3 px-4 text-[#8D6E63]">{item.supplier}</td>
                  <td className="py-3 px-4 flex items-center gap-2">
                    <button className="p-1 rounded hover:bg-[#F3EDE9]"><Edit size={14} style={{ color: BROWN }} /></button>
                    {low && <AccentBtn small>Reorder</AccentBtn>}
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

function AttendanceScreen() {
  const now = new Date();
  return (
    <div className="p-6">
      <h2 className="font-bold text-[#1A1210] font-[Poppins] mb-6">Attendance Management</h2>
      <div className="grid grid-cols-3 gap-6">
        <Card className="p-6 text-center" style={{ background: DARK_BROWN }}>
          <p className="text-[#D7CCC8] text-sm mb-2">Current Time</p>
          <p className="text-white text-3xl font-bold font-[Poppins]">{now.toLocaleTimeString("en-ZA", { hour: "2-digit", minute: "2-digit" })}</p>
          <p className="text-[#D7CCC8] text-sm mt-1">{now.toLocaleDateString("en-ZA", { weekday: "long", day: "numeric", month: "long" })}</p>
          <div className="flex gap-3 mt-5">
            <button className="flex-1 py-2.5 rounded-lg font-semibold text-white text-sm" style={{ background: GREEN }}>Clock In</button>
            <button className="flex-1 py-2.5 rounded-lg font-semibold text-white text-sm" style={{ background: RED }}>Clock Out</button>
          </div>
        </Card>
        <div className="col-span-2">
          <Card className="overflow-hidden">
            <div className="px-4 py-3 border-b border-[rgba(78,52,46,0.08)] font-semibold text-[#1A1210] font-[Poppins]">Today's Attendance</div>
            <table className="w-full text-sm">
              <thead>
                <tr className="border-b border-[rgba(78,52,46,0.08)]" style={{ background: "#F3EDE9" }}>
                  {["Staff Member", "Role", "Clock In", "Clock Out", "Hours", "Status"].map(h => (
                    <th key={h} className="text-left py-2 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide">{h}</th>
                  ))}
                </tr>
              </thead>
              <tbody>
                {staff.map(s => (
                  <tr key={s.id} className="border-b border-[rgba(78,52,46,0.06)]">
                    <td className="py-3 px-4 font-medium text-[#1A1210]">{s.name}</td>
                    <td className="py-3 px-4 text-[#8D6E63]">{s.role}</td>
                    <td className="py-3 px-4 text-[#1A1210]">{s.status === "Active" ? "07:02" : "—"}</td>
                    <td className="py-3 px-4 text-[#8D6E63]">{s.status === "Off-duty" ? "15:01" : "—"}</td>
                    <td className="py-3 px-4">{s.status === "Off-duty" ? "7h 59m" : s.status === "Active" ? "Ongoing" : "—"}</td>
                    <td className="py-3 px-4"><Badge color={s.status === "Active" ? "green" : "gray"}>{s.status}</Badge></td>
                  </tr>
                ))}
              </tbody>
            </table>
          </Card>
        </div>
      </div>
    </div>
  );
}

function ScheduleScreen() {
  const days = ["Mon 23", "Tue 24", "Wed 25", "Thu 26", "Fri 27", "Sat 28", "Sun 29"];
  const shifts = [
    { staff: "Thabo Nkosi", color: "bg-amber-100 text-amber-800", role: "Barista", slots: [1, 1, 1, 0, 1, 0, 0] },
    { staff: "Lerato Dlamini", color: "bg-blue-100 text-blue-800", role: "Cashier", slots: [1, 1, 0, 1, 1, 1, 0] },
    { staff: "Sipho Mokoena", color: "bg-green-100 text-green-800", role: "Supervisor", slots: [1, 1, 1, 1, 1, 0, 0] },
    { staff: "Zanele Khumalo", color: "bg-purple-100 text-purple-800", role: "Barista", slots: [0, 0, 1, 1, 1, 1, 1] },
    { staff: "Amahle Cele", color: "bg-pink-100 text-pink-800", role: "Kitchen", slots: [1, 1, 1, 0, 1, 1, 0] },
  ];
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-bold text-[#1A1210] font-[Poppins]">Schedule — Week of 23 Jun 2025</h2>
        <AccentBtn small>Edit Schedule</AccentBtn>
      </div>
      <Card className="overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-[rgba(78,52,46,0.08)]" style={{ background: "#F3EDE9" }}>
              <th className="text-left py-3 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide w-40">Staff</th>
              {days.map(d => <th key={d} className="py-3 px-3 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide text-center">{d}</th>)}
            </tr>
          </thead>
          <tbody>
            {shifts.map((s, i) => (
              <tr key={i} className="border-b border-[rgba(78,52,46,0.06)]">
                <td className="py-3 px-4">
                  <p className="font-medium text-[#1A1210]">{s.staff}</p>
                  <p className="text-xs text-[#8D6E63]">{s.role}</p>
                </td>
                {s.slots.map((on, j) => (
                  <td key={j} className="py-3 px-3 text-center">
                    {on ? (
                      <span className={`text-xs font-semibold px-2 py-1 rounded-lg ${s.color}`}>07:00–15:00</span>
                    ) : (
                      <span className="text-xs text-[#8D6E63]">Off</span>
                    )}
                  </td>
                ))}
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

// ─── SECTION: MANAGER DASHBOARD ───────────────────────────────────────────────
function ManagerSidebar({ active, setActive }: any) {
  const links = [
    { id: "mgrdash", label: "Dashboard", icon: BarChart2 },
    { id: "staffmgmt", label: "Staff Management", icon: Users },
    { id: "suppliers", label: "Suppliers", icon: Truck },
    { id: "eventmgmt", label: "Event Management", icon: Calendar },
    { id: "reports", label: "Reports", icon: FileText },
    { id: "analytics", label: "Analytics", icon: Activity },
  ];
  return (
    <aside className="w-60 flex-shrink-0 flex flex-col" style={{ background: DARK_BROWN, minHeight: "100vh" }}>
      <div className="px-6 py-5 border-b border-white/10">
        <div className="flex items-center gap-2">
          <Coffee size={22} style={{ color: ORANGE }} />
          <div>
            <p className="text-white font-bold text-sm font-[Poppins]">Skinner Cafe</p>
            <p className="text-[#D7CCC8] text-xs">Manager Portal</p>
          </div>
        </div>
      </div>
      <nav className="flex-1 px-3 py-4">
        {links.map(l => (
          <button key={l.id} onClick={() => setActive(l.id)} className={`w-full flex items-center gap-3 px-3 py-2.5 rounded-lg mb-1 text-sm font-medium transition-all text-left ${active === l.id ? "text-white" : "text-[#D7CCC8] hover:bg-white/10"}`} style={active === l.id ? { background: BROWN } : {}}>
            <l.icon size={16} />
            {l.label}
          </button>
        ))}
      </nav>
      <div className="px-3 pb-4 border-t border-white/10 pt-4">
        <div className="flex items-center gap-3 px-3 py-2">
          <div className="w-8 h-8 rounded-full bg-orange-400/20 flex items-center justify-center"><User size={14} style={{ color: ORANGE }} /></div>
          <div className="flex-1 min-w-0">
            <p className="text-white text-xs font-semibold truncate">Nomvula Khumalo</p>
            <p className="text-[#D7CCC8] text-xs">Manager</p>
          </div>
          <LogOut size={14} className="text-[#D7CCC8]" />
        </div>
      </div>
    </aside>
  );
}

function ManagerDashContent() {
  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h1 className="text-xl font-bold text-[#1A1210] font-[Poppins]">Manager Dashboard</h1>
        <div className="flex items-center gap-2 text-sm text-[#8D6E63]"><Calendar size={14} />24 June 2025</div>
      </div>
      <div className="grid grid-cols-4 gap-4">
        <StatCard icon={DollarSign} label="Revenue Today" value="R24,580" trend="+18%" />
        <StatCard icon={ShoppingBag} label="Orders Today" value="347" trend="+12%" />
        <StatCard icon={Users} label="Staff On Shift" value="4 / 6" sub="2 off duty" />
        <StatCard icon={AlertTriangle} label="Inventory Alerts" value="3" sub="Requires attention" />
      </div>
      <div className="grid grid-cols-2 gap-6">
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Weekly Revenue</h3>
          <ResponsiveContainer width="100%" height={200}>
            <AreaChart data={revenueData}>
              <defs>
                <linearGradient id="rev" x1="0" y1="0" x2="0" y2="1">
                  <stop offset="5%" stopColor={ORANGE} stopOpacity={0.2} />
                  <stop offset="95%" stopColor={ORANGE} stopOpacity={0} />
                </linearGradient>
              </defs>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(78,52,46,0.08)" />
              <XAxis dataKey="day" tick={{ fontSize: 12, fill: MUTED }} />
              <YAxis tick={{ fontSize: 12, fill: MUTED }} tickFormatter={v => `R${(v/1000).toFixed(0)}k`} />
              <Tooltip formatter={(v: number) => [`R${v.toLocaleString()}`, "Revenue"]} />
              <Area type="monotone" dataKey="revenue" stroke={ORANGE} strokeWidth={2} fill="url(#rev)" />
            </AreaChart>
          </ResponsiveContainer>
        </Card>
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Sales by Category</h3>
          <div className="flex items-center gap-6">
            <ResponsiveContainer width={160} height={160}>
              <RechartsPie>
                <Pie data={salesCatData} cx="50%" cy="50%" innerRadius={45} outerRadius={75} dataKey="value">
                  {salesCatData.map((_, i) => <Cell key={i} fill={PIE_COLORS[i % PIE_COLORS.length]} />)}
                </Pie>
              </RechartsPie>
            </ResponsiveContainer>
            <div className="space-y-2">
              {salesCatData.map((d, i) => (
                <div key={d.name} className="flex items-center gap-2 text-sm">
                  <span className="w-3 h-3 rounded-full flex-shrink-0" style={{ background: PIE_COLORS[i] }} />
                  <span className="text-[#8D6E63]">{d.name}</span>
                  <span className="font-semibold text-[#1A1210] ml-auto">{d.value}%</span>
                </div>
              ))}
            </div>
          </div>
        </Card>
      </div>
      <Card className="p-5">
        <div className="flex items-center justify-between mb-4">
          <h3 className="font-semibold text-[#1A1210] font-[Poppins]">Top Products This Week</h3>
        </div>
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-[rgba(78,52,46,0.08)]">
              {["Product", "Units Sold", "Revenue", "Share"].map(h => <th key={h} className="text-left py-2 text-xs font-semibold text-[#8D6E63] uppercase tracking-wide">{h}</th>)}
            </tr>
          </thead>
          <tbody>
            {topProducts.map((p, i) => (
              <tr key={p.name} className="border-b border-[rgba(78,52,46,0.05)]">
                <td className="py-3 flex items-center gap-2">
                  <span className="w-5 h-5 rounded-full text-xs font-bold flex items-center justify-center text-white" style={{ background: BROWN }}>{i + 1}</span>
                  {p.name}
                </td>
                <td className="py-3 font-medium">{p.sales}</td>
                <td className="py-3 font-semibold text-[#4E342E]">R{p.revenue.toLocaleString()}</td>
                <td className="py-3">
                  <div className="flex items-center gap-2">
                    <div className="flex-1 h-1.5 rounded-full bg-[#F3EDE9] overflow-hidden">
                      <div className="h-full rounded-full" style={{ width: `${(p.revenue / 15000) * 100}%`, background: ORANGE }} />
                    </div>
                  </div>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

function StaffMgmtScreen() {
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-bold text-[#1A1210] font-[Poppins]">Staff Management</h2>
        <AccentBtn><Plus size={14} className="inline mr-1" />Add Employee</AccentBtn>
      </div>
      <Card className="overflow-hidden">
        <table className="w-full text-sm">
          <thead>
            <tr className="border-b border-[rgba(78,52,46,0.08)]" style={{ background: "#F3EDE9" }}>
              {["Employee", "Role", "Shift", "Joined", "Status", "Actions"].map(h => (
                <th key={h} className="text-left py-3 px-4 text-xs font-semibold text-[#8D6E63] uppercase tracking-wider">{h}</th>
              ))}
            </tr>
          </thead>
          <tbody>
            {staff.map(s => (
              <tr key={s.id} className="border-b border-[rgba(78,52,46,0.06)] hover:bg-[#FAFAFA]">
                <td className="py-3 px-4">
                  <div className="flex items-center gap-3">
                    <div className="w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-xs" style={{ background: BROWN }}>{s.name.charAt(0)}</div>
                    <span className="font-medium text-[#1A1210]">{s.name}</span>
                  </div>
                </td>
                <td className="py-3 px-4 text-[#8D6E63]">{s.role}</td>
                <td className="py-3 px-4 text-[#8D6E63]">{s.shift}</td>
                <td className="py-3 px-4 text-[#8D6E63]">{s.joined}</td>
                <td className="py-3 px-4"><Badge color={s.status === "Active" ? "green" : "gray"}>{s.status}</Badge></td>
                <td className="py-3 px-4 flex items-center gap-2">
                  <button className="p-1.5 rounded-lg hover:bg-[#F3EDE9]"><Edit size={14} style={{ color: BROWN }} /></button>
                  <button className="p-1.5 rounded-lg hover:bg-red-50"><Trash2 size={14} className="text-red-400" /></button>
                </td>
              </tr>
            ))}
          </tbody>
        </table>
      </Card>
    </div>
  );
}

function SupplierScreen() {
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-bold text-[#1A1210] font-[Poppins]">Supplier Management</h2>
        <AccentBtn><Plus size={14} className="inline mr-1" />Add Supplier</AccentBtn>
      </div>
      <div className="grid grid-cols-2 gap-4">
        {suppliers.map((s, i) => (
          <Card key={i} className="p-5">
            <div className="flex items-start justify-between mb-3">
              <div>
                <p className="font-semibold text-[#1A1210] font-[Poppins]">{s.name}</p>
                <Badge color={s.status === "Active" ? "green" : "gray"}>{s.status}</Badge>
              </div>
              <div className="flex gap-1">
                <button className="p-1.5 rounded-lg hover:bg-[#F3EDE9]"><Edit size={14} style={{ color: BROWN }} /></button>
                <button className="p-1.5 rounded-lg hover:bg-red-50"><Eye size={14} className="text-[#8D6E63]" /></button>
              </div>
            </div>
            <div className="space-y-1.5 text-sm">
              <p className="flex items-center gap-2 text-[#8D6E63]"><Mail size={13} />{s.contact}</p>
              <p className="flex items-center gap-2 text-[#8D6E63]"><Phone size={13} />{s.phone}</p>
              <p className="flex items-center gap-2 text-[#8D6E63]"><Package size={13} />{s.items} product lines</p>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

function EventMgmtScreen() {
  return (
    <div className="p-6">
      <div className="flex items-center justify-between mb-6">
        <h2 className="font-bold text-[#1A1210] font-[Poppins]">Event Management</h2>
        <AccentBtn><Plus size={14} className="inline mr-1" />Create Event</AccentBtn>
      </div>
      <div className="grid grid-cols-2 gap-5">
        {events.map(e => (
          <Card key={e.id} className="overflow-hidden">
            <div className="h-40 overflow-hidden relative">
              <img src={`https://images.unsplash.com/photo-${e.img}?w=600&h=200&fit=crop&auto=format`} alt={e.title} className="w-full h-full object-cover" />
              <span className="absolute top-3 right-3 text-xs font-bold text-white px-3 py-1 rounded-full" style={{ background: ORANGE }}>{e.price}</span>
            </div>
            <div className="p-4">
              <p className="font-semibold text-[#1A1210] font-[Poppins]">{e.title}</p>
              <div className="flex items-center gap-3 text-xs text-[#8D6E63] mt-2">
                <span className="flex items-center gap-1"><Calendar size={11} />{e.date}</span>
                <span className="flex items-center gap-1"><Clock size={11} />{e.time}</span>
                <span className="flex items-center gap-1"><Users size={11} />{e.spots} spots left</span>
              </div>
              <div className="mt-3 flex items-center justify-between">
                <div>
                  <p className="text-xs text-[#8D6E63]">RSVPs</p>
                  <p className="font-bold text-[#4E342E]">{40 - e.spots} / 40</p>
                </div>
                <div>
                  <p className="text-xs text-[#8D6E63]">Revenue</p>
                  <p className="font-bold text-[#4E342E]">{e.price === "Free" ? "—" : `R${(40 - e.spots) * parseInt(e.price.replace("R", ""))}`}</p>
                </div>
                <div className="flex gap-2">
                  <OutlineBtn small>Edit</OutlineBtn>
                </div>
              </div>
            </div>
          </Card>
        ))}
      </div>
    </div>
  );
}

function ReportsScreen() {
  return (
    <div className="p-6 space-y-6">
      <div className="flex items-center justify-between">
        <h2 className="font-bold text-[#1A1210] font-[Poppins]">Reports Centre</h2>
        <div className="flex gap-2">
          <OutlineBtn small><Download size={13} className="inline mr-1" />Export PDF</OutlineBtn>
          <OutlineBtn small><Download size={13} className="inline mr-1" />Export CSV</OutlineBtn>
        </div>
      </div>
      <div className="grid grid-cols-2 gap-5">
        {[
          { title: "Sales Report", icon: DollarSign, sub: "R49,420 this week · +15% vs last week", color: "bg-orange-50 border-orange-100" },
          { title: "Inventory Report", icon: Package, sub: "3 low-stock alerts · Last updated today", color: "bg-amber-50 border-amber-100" },
          { title: "Attendance Report", icon: UserCheck, sub: "96% attendance rate this week", color: "bg-green-50 border-green-100" },
          { title: "Events Report", icon: Calendar, sub: "4 events this month · R12,360 revenue", color: "bg-blue-50 border-blue-100" },
        ].map((r, i) => (
          <Card key={i} className={`p-5 flex items-center gap-4 cursor-pointer hover:shadow-md transition-all border ${r.color}`}>
            <div className="w-12 h-12 rounded-xl flex items-center justify-center" style={{ background: `${ORANGE}20` }}>
              <r.icon size={22} style={{ color: ORANGE }} />
            </div>
            <div className="flex-1">
              <p className="font-semibold text-[#1A1210] font-[Poppins]">{r.title}</p>
              <p className="text-sm text-[#8D6E63] mt-0.5">{r.sub}</p>
            </div>
            <ChevronRight size={18} className="text-[#8D6E63]" />
          </Card>
        ))}
      </div>
      <Card className="p-5">
        <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Weekly Orders Volume</h3>
        <ResponsiveContainer width="100%" height={200}>
          <BarChart data={revenueData}>
            <CartesianGrid strokeDasharray="3 3" stroke="rgba(78,52,46,0.08)" />
            <XAxis dataKey="day" tick={{ fontSize: 12, fill: MUTED }} />
            <YAxis tick={{ fontSize: 12, fill: MUTED }} />
            <Tooltip />
            <Bar dataKey="orders" fill={BROWN} radius={[4, 4, 0, 0]} />
          </BarChart>
        </ResponsiveContainer>
      </Card>
    </div>
  );
}

function AnalyticsScreen() {
  return (
    <div className="p-6 space-y-6">
      <h2 className="font-bold text-[#1A1210] font-[Poppins]">Analytics Dashboard</h2>
      <div className="grid grid-cols-2 gap-6">
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Revenue Trends (Weekly)</h3>
          <ResponsiveContainer width="100%" height={200}>
            <LineChart data={revenueData}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(78,52,46,0.08)" />
              <XAxis dataKey="day" tick={{ fontSize: 12, fill: MUTED }} />
              <YAxis tick={{ fontSize: 12, fill: MUTED }} tickFormatter={v => `R${(v/1000).toFixed(0)}k`} />
              <Tooltip formatter={(v: number) => [`R${v.toLocaleString()}`, "Revenue"]} />
              <Line type="monotone" dataKey="revenue" stroke={ORANGE} strokeWidth={2.5} dot={{ fill: ORANGE, strokeWidth: 0, r: 4 }} />
            </LineChart>
          </ResponsiveContainer>
        </Card>
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Peak Hours Analysis</h3>
          <ResponsiveContainer width="100%" height={200}>
            <BarChart data={peakHours}>
              <CartesianGrid strokeDasharray="3 3" stroke="rgba(78,52,46,0.08)" />
              <XAxis dataKey="hour" tick={{ fontSize: 10, fill: MUTED }} interval={1} />
              <YAxis tick={{ fontSize: 11, fill: MUTED }} />
              <Tooltip />
              <Bar dataKey="visits" fill={BROWN} radius={[3, 3, 0, 0]} />
            </BarChart>
          </ResponsiveContainer>
        </Card>
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Best-Selling Products</h3>
          <div className="space-y-3">
            {topProducts.map((p, i) => (
              <div key={p.name} className="flex items-center gap-3">
                <span className="text-xs font-bold w-5 text-[#8D6E63]">{i + 1}</span>
                <span className="flex-1 text-sm text-[#1A1210] font-medium">{p.name}</span>
                <div className="w-32 h-2 rounded-full bg-[#F3EDE9] overflow-hidden">
                  <div className="h-full rounded-full" style={{ width: `${(p.sales / 320) * 100}%`, background: i === 0 ? ORANGE : BROWN }} />
                </div>
                <span className="text-sm font-semibold text-[#4E342E] w-10 text-right">{p.sales}</span>
              </div>
            ))}
          </div>
        </Card>
        <Card className="p-5">
          <h3 className="font-semibold text-[#1A1210] mb-4 font-[Poppins]">Customer Loyalty Stats</h3>
          <div className="grid grid-cols-2 gap-4">
            {[
              { label: "Active Members", value: "1,248", trend: "+24 this month" },
              { label: "Points Redeemed", value: "42,800", trend: "R2,140 value" },
              { label: "Gold Members", value: "312", trend: "25% of base" },
              { label: "Retention Rate", value: "78%", trend: "+5% vs last qtr" },
            ].map((s, i) => (
              <div key={i} className="p-3 rounded-xl" style={{ background: "#F3EDE9" }}>
                <p className="text-xs text-[#8D6E63]">{s.label}</p>
                <p className="text-xl font-bold text-[#4E342E] font-[Poppins]">{s.value}</p>
                <p className="text-xs text-[#8D6E63] mt-0.5">{s.trend}</p>
              </div>
            ))}
          </div>
        </Card>
      </div>
    </div>
  );
}

// ─── SECTION: MOBILE MOCKUPS ──────────────────────────────────────────────────
function PhoneFrame({ children, label }: { children: ReactNode; label: string }) {
  return (
    <div className="flex flex-col items-center gap-2">
      <div className="relative" style={{ width: 220, height: 460 }}>
        <div className="absolute inset-0 rounded-[2.5rem] border-4 border-[#1A1210] shadow-2xl overflow-hidden bg-white">
          <div className="absolute top-0 inset-x-0 h-7 flex items-center justify-center z-10" style={{ background: "#1A1210" }}>
            <div className="w-20 h-4 rounded-full bg-black" />
          </div>
          <div className="absolute bottom-0 inset-x-0 h-5 flex items-center justify-center z-10" style={{ background: "#1A1210" }}>
            <div className="w-16 h-1 rounded-full bg-white/40" />
          </div>
          <div className="absolute inset-0 pt-7 pb-5 overflow-hidden">
            {children}
          </div>
        </div>
      </div>
      <p className="text-xs font-semibold text-[#8D6E63] text-center">{label}</p>
    </div>
  );
}

function MobileStatusBar({ dark = false }) {
  return (
    <div className={`flex items-center justify-between px-4 py-1 text-xs ${dark ? "text-white" : "text-[#1A1210]"}`}>
      <span className="font-semibold">9:41</span>
      <div className="flex items-center gap-1">
        <Signal size={12} /><Wifi size={12} /><Battery size={12} />
      </div>
    </div>
  );
}

function MobileBottomNav({ active }: { active: string }) {
  const tabs = [
    { id: "home", icon: Home, label: "Home" },
    { id: "menu", icon: Coffee, label: "Menu" },
    { id: "cart", icon: ShoppingCart, label: "Cart" },
    { id: "events", icon: Calendar, label: "Events" },
    { id: "profile", icon: User, label: "Profile" },
  ];
  return (
    <div className="absolute bottom-0 inset-x-0 bg-white border-t border-[rgba(78,52,46,0.1)] flex pb-1">
      {tabs.map(t => (
        <button key={t.id} className="flex-1 flex flex-col items-center py-1.5 gap-0.5">
          <t.icon size={16} style={{ color: active === t.id ? ORANGE : MUTED }} />
          <span className="text-[8px] font-medium" style={{ color: active === t.id ? ORANGE : MUTED }}>{t.label}</span>
        </button>
      ))}
    </div>
  );
}

function MobileSplash() {
  return (
    <div className="h-full flex flex-col items-center justify-center" style={{ background: DARK_BROWN }}>
      <MobileStatusBar dark />
      <div className="flex-1 flex flex-col items-center justify-center gap-4">
        <div className="w-20 h-20 rounded-3xl flex items-center justify-center mb-2" style={{ background: BROWN }}>
          <Coffee size={40} style={{ color: ORANGE }} />
        </div>
        <p className="text-white font-bold text-2xl font-[Poppins]">Skinner Cafe</p>
        <p className="text-[#D7CCC8] text-xs">Braamfontein, Johannesburg</p>
        <div className="mt-8 flex gap-1">
          {[1, 2, 3].map(i => <div key={i} className="w-1.5 h-1.5 rounded-full" style={{ background: i === 1 ? ORANGE : "rgba(255,255,255,0.3)" }} />)}
        </div>
      </div>
    </div>
  );
}

function MobileLogin() {
  return (
    <div className="h-full flex flex-col bg-[#FAFAFA]">
      <MobileStatusBar />
      <div className="flex-1 overflow-auto px-5 pb-16">
        <div className="flex items-center justify-center gap-1.5 mt-6 mb-6">
          <Coffee size={20} style={{ color: ORANGE }} />
          <span className="font-bold text-base font-[Poppins]" style={{ color: BROWN }}>Skinner Cafe</span>
        </div>
        <p className="text-base font-bold text-[#1A1210] font-[Poppins]">Welcome back</p>
        <p className="text-xs text-[#8D6E63] mb-5">Sign in to your account</p>
        <div className="space-y-3">
          <div>
            <label className="text-xs font-medium text-[#1A1210]">Email</label>
            <input className="w-full mt-1 px-3 py-2 rounded-lg bg-[#F3EDE9] border-0 text-xs focus:outline-none" placeholder="hello@example.co.za" />
          </div>
          <div>
            <label className="text-xs font-medium text-[#1A1210]">Password</label>
            <input type="password" className="w-full mt-1 px-3 py-2 rounded-lg bg-[#F3EDE9] border-0 text-xs focus:outline-none" placeholder="••••••••" />
          </div>
          <button className="text-xs font-semibold" style={{ color: ORANGE }}>Forgot password?</button>
          <button className="w-full py-2.5 rounded-xl text-white text-xs font-semibold" style={{ background: BROWN }}>Sign In</button>
          <p className="text-[10px] text-center text-[#8D6E63]">No account? <span className="font-semibold" style={{ color: BROWN }}>Register</span></p>
        </div>
      </div>
    </div>
  );
}

function MobileHome() {
  return (
    <div className="h-full flex flex-col bg-[#FAFAFA]">
      <div style={{ background: DARK_BROWN }}>
        <MobileStatusBar dark />
        <div className="px-4 pb-4">
          <div className="flex items-center justify-between mb-3">
            <div>
              <p className="text-[#D7CCC8] text-[10px]">Good morning,</p>
              <p className="text-white font-bold text-base font-[Poppins]">Lerato 👋</p>
            </div>
            <div className="relative">
              <ShoppingCart size={20} color="white" />
              <span className="absolute -top-1 -right-1 w-3.5 h-3.5 rounded-full flex items-center justify-center text-[8px] font-bold text-white" style={{ background: ORANGE }}>3</span>
            </div>
          </div>
          <div className="relative">
            <Search size={12} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#8D6E63]" />
            <input placeholder="Search menu, events…" className="w-full pl-8 pr-3 py-2 rounded-xl bg-white text-[10px] focus:outline-none" />
          </div>
        </div>
      </div>
      <div className="flex-1 overflow-auto pb-14 px-3 pt-3 space-y-3">
        {/* Hero card */}
        <div className="relative h-28 rounded-2xl overflow-hidden">
          <img src="https://images.unsplash.com/photo-1495474472287-4d71bcdd2085?w=400&h=200&fit=crop&auto=format" alt="Cafe" className="w-full h-full object-cover" />
          <div className="absolute inset-0 flex flex-col justify-end p-3" style={{ background: "linear-gradient(to top, rgba(62,39,35,0.85), transparent)" }}>
            <p className="text-white font-bold text-xs font-[Poppins]">Jazz Night — Fri 27 Jun</p>
            <p className="text-[#D7CCC8] text-[9px]">20:00 · R120</p>
          </div>
        </div>
        {/* Loyalty */}
        <div className="rounded-xl p-3 flex items-center gap-3" style={{ background: "#F3EDE9" }}>
          <Award size={20} style={{ color: ORANGE }} />
          <div className="flex-1">
            <p className="text-xs font-semibold text-[#4E342E]">1,240 Loyalty Points</p>
            <div className="h-1.5 rounded-full bg-white mt-1 overflow-hidden"><div className="h-full rounded-full w-3/5" style={{ background: ORANGE }} /></div>
          </div>
          <span className="text-[9px] text-[#8D6E63]">760 to Platinum</span>
        </div>
        {/* Menu */}
        <p className="text-xs font-bold text-[#1A1210] font-[Poppins]">Fan Favourites</p>
        {menuItems.slice(0, 3).map(item => (
          <div key={item.id} className="flex items-center gap-3 bg-white rounded-xl p-2.5 shadow-sm">
            <img src={`https://images.unsplash.com/photo-${item.img}?w=80&h=80&fit=crop&auto=format`} alt={item.name} className="w-12 h-12 rounded-xl object-cover flex-shrink-0" />
            <div className="flex-1">
              <p className="text-xs font-semibold text-[#1A1210]">{item.name}</p>
              <p className="text-[9px] text-[#8D6E63]">{item.cat}</p>
            </div>
            <div className="text-right">
              <p className="text-xs font-bold text-[#4E342E]">R{item.price}</p>
              <button className="mt-1 w-6 h-6 rounded-full flex items-center justify-center text-white" style={{ background: ORANGE }}><Plus size={10} /></button>
            </div>
          </div>
        ))}
      </div>
      <MobileBottomNav active="home" />
    </div>
  );
}

function MobileMenu() {
  return (
    <div className="h-full flex flex-col bg-[#FAFAFA]">
      <MobileStatusBar />
      <div className="px-4 pb-3">
        <p className="font-bold text-sm text-[#1A1210] font-[Poppins]">Menu</p>
        <div className="relative mt-2">
          <Search size={12} className="absolute left-3 top-1/2 -translate-y-1/2 text-[#8D6E63]" />
          <input placeholder="Search…" className="w-full pl-8 pr-3 py-2 rounded-xl bg-[#F3EDE9] text-[10px] focus:outline-none" />
        </div>
        <div className="flex gap-2 mt-2 overflow-x-auto pb-1">
          {["All", "Hot Coffee", "Cold", "Pastries"].map((c, i) => (
            <button key={c} className={`px-3 py-1 rounded-full text-[9px] font-semibold whitespace-nowrap ${i === 0 ? "text-white" : "bg-white border border-[rgba(78,52,46,0.15)] text-[#8D6E63]"}`} style={i === 0 ? { background: BROWN } : {}}>
              {c}
            </button>
          ))}
        </div>
      </div>
      <div className="flex-1 overflow-auto pb-14 px-3 grid grid-cols-2 gap-2 content-start">
        {menuItems.slice(0, 6).map(item => (
          <div key={item.id} className="bg-white rounded-xl overflow-hidden shadow-sm">
            <img src={`https://images.unsplash.com/photo-${item.img}?w=200&h=120&fit=crop&auto=format`} alt={item.name} className="w-full h-20 object-cover" />
            <div className="p-2">
              <p className="text-[10px] font-semibold text-[#1A1210]">{item.name}</p>
              <div className="flex items-center justify-between mt-1">
                <span className="text-[10px] font-bold text-[#4E342E]">R{item.price}</span>
                <button className="w-5 h-5 rounded-full flex items-center justify-center text-white" style={{ background: ORANGE }}><Plus size={8} /></button>
              </div>
            </div>
          </div>
        ))}
      </div>
      <MobileBottomNav active="menu" />
    </div>
  );
}

function MobileCart() {
  return (
    <div className="h-full flex flex-col bg-[#FAFAFA]">
      <MobileStatusBar />
      <div className="px-4 pb-3">
        <p className="font-bold text-sm text-[#1A1210] font-[Poppins]">Your Cart</p>
        <p className="text-[10px] text-[#8D6E63]">3 items</p>
      </div>
      <div className="flex-1 overflow-auto pb-28 px-3 space-y-2">
        {[{ name: "Cappuccino", price: 45, qty: 2 }, { name: "Avocado Toast", price: 95, qty: 1 }].map((item, i) => (
          <div key={i} className="flex items-center gap-3 bg-white rounded-xl p-2.5 shadow-sm">
            <div className="w-10 h-10 rounded-xl bg-amber-50 flex-shrink-0" />
            <div className="flex-1">
              <p className="text-xs font-semibold text-[#1A1210]">{item.name}</p>
              <p className="text-[9px] text-[#8D6E63]">R{item.price} each</p>
            </div>
            <div className="flex items-center gap-1">
              <button className="w-5 h-5 rounded-full bg-[#F3EDE9] flex items-center justify-center"><Minus size={8} /></button>
              <span className="text-[10px] font-bold w-4 text-center">{item.qty}</span>
              <button className="w-5 h-5 rounded-full bg-[#F3EDE9] flex items-center justify-center"><Plus size={8} /></button>
            </div>
            <p className="text-xs font-bold text-[#4E342E]">R{item.price * item.qty}</p>
          </div>
        ))}
      </div>
      <div className="absolute bottom-14 inset-x-3 bg-white rounded-2xl shadow-lg p-4">
        <div className="flex justify-between text-xs mb-1"><span className="text-[#8D6E63]">Subtotal</span><span>R185</span></div>
        <div className="flex justify-between text-xs mb-2"><span className="text-[#8D6E63]">Service fee</span><span>R10</span></div>
        <div className="flex justify-between font-bold text-sm mb-3 text-[#1A1210]"><span>Total</span><span>R195</span></div>
        <button className="w-full py-2.5 rounded-xl text-white text-xs font-semibold" style={{ background: ORANGE }}>Checkout</button>
      </div>
      <MobileBottomNav active="cart" />
    </div>
  );
}

function MobileLoyalty() {
  return (
    <div className="h-full flex flex-col bg-[#FAFAFA]">
      <MobileStatusBar />
      <div className="flex-1 overflow-auto pb-14">
        <div className="p-4 text-center" style={{ background: DARK_BROWN }}>
          <Award size={32} style={{ color: ORANGE }} className="mx-auto mb-2" />
          <p className="text-white font-bold font-[Poppins]">Gold Member</p>
          <p className="text-[#D7CCC8] text-[10px] mt-0.5">Lerato Dlamini</p>
          <p className="text-4xl font-bold text-white mt-3 font-[Poppins]">1,240</p>
          <p className="text-[#D7CCC8] text-xs">Points Available</p>
          <div className="mt-3 h-2 rounded-full bg-white/20 mx-4 overflow-hidden">
            <div className="h-full rounded-full w-3/5" style={{ background: ORANGE }} />
          </div>
          <p className="text-[10px] text-[#D7CCC8] mt-1">760 pts to Platinum</p>
        </div>
        <div className="px-4 mt-4 grid grid-cols-2 gap-2">
          {[{ label: "Orders", val: "47" }, { label: "Total Spent", val: "R4,820" }, { label: "Redeemed", val: "3,200 pts" }, { label: "Since", val: "Jan 2024" }].map((s, i) => (
            <div key={i} className="bg-white rounded-xl p-3 shadow-sm">
              <p className="text-[9px] text-[#8D6E63]">{s.label}</p>
              <p className="text-sm font-bold text-[#4E342E] font-[Poppins]">{s.val}</p>
            </div>
          ))}
        </div>
        <div className="px-4 mt-4">
          <p className="text-xs font-bold text-[#1A1210] font-[Poppins] mb-2">Redeem Points</p>
          {[{ reward: "Free Espresso", pts: 200 }, { reward: "Free Cappuccino", pts: 400 }, { reward: "Event Ticket", pts: 600 }].map((r, i) => (
            <div key={i} className="flex items-center justify-between bg-white rounded-xl p-3 shadow-sm mb-2">
              <div>
                <p className="text-xs font-medium text-[#1A1210]">{r.reward}</p>
                <p className="text-[9px] text-[#8D6E63]">{r.pts} pts</p>
              </div>
              <button className="px-3 py-1.5 rounded-lg text-[9px] font-semibold text-white" style={{ background: ORANGE }}>Redeem</button>
            </div>
          ))}
        </div>
      </div>
      <MobileBottomNav active="profile" />
    </div>
  );
}

function StaffMobileOrders() {
  return (
    <div className="h-full flex flex-col" style={{ background: "#FAFAFA" }}>
      <div style={{ background: DARK_BROWN }}>
        <MobileStatusBar dark />
        <div className="px-4 pb-3 flex items-center justify-between">
          <p className="text-white font-bold text-sm font-[Poppins]">Order Queue</p>
          <span className="px-2 py-0.5 rounded-full text-[9px] font-bold text-white" style={{ background: ORANGE }}>5 Active</span>
        </div>
        <div className="flex px-4 pb-3 gap-2">
          {["All", "Received", "Preparing", "Ready"].map((s, i) => (
            <button key={s} className={`px-3 py-1 rounded-full text-[9px] font-semibold ${i === 0 ? "text-white" : "bg-white/10 text-[#D7CCC8]"}`} style={i === 0 ? { background: ORANGE } : {}}>
              {s}
            </button>
          ))}
        </div>
      </div>
      <div className="flex-1 overflow-auto px-3 pt-3 space-y-2">
        {orders.map(o => (
          <div key={o.id} className="bg-white rounded-xl p-3 shadow-sm">
            <div className="flex justify-between items-start">
              <div>
                <p className="text-xs font-bold text-[#4E342E]">{o.id}</p>
                <p className="text-[10px] text-[#8D6E63]">{o.customer}</p>
              </div>
              <Badge color={statusColor(o.status)}>{o.status}</Badge>
            </div>
            <p className="text-[10px] text-[#1A1210] mt-1">{o.items}</p>
            <div className="flex items-center justify-between mt-2">
              <p className="text-[9px] text-[#8D6E63]">{o.time}</p>
              {o.status !== "Completed" && (
                <button className="px-3 py-1 rounded-lg text-[9px] font-semibold text-white" style={{ background: BROWN }}>Update</button>
              )}
            </div>
          </div>
        ))}
      </div>
    </div>
  );
}

function ManagerMobileDash() {
  return (
    <div className="h-full flex flex-col" style={{ background: "#FAFAFA" }}>
      <div style={{ background: DARK_BROWN }}>
        <MobileStatusBar dark />
        <div className="px-4 pb-4">
          <p className="text-[#D7CCC8] text-[10px]">Manager Dashboard</p>
          <p className="text-white font-bold text-sm font-[Poppins]">Nomvula Khumalo</p>
        </div>
      </div>
      <div className="flex-1 overflow-auto px-3 pt-3 space-y-3 pb-4">
        <div className="grid grid-cols-2 gap-2">
          {[
            { l: "Revenue Today", v: "R24,580", icon: DollarSign },
            { l: "Orders", v: "347", icon: ShoppingBag },
            { l: "Staff Active", v: "4 / 6", icon: Users },
            { l: "Alerts", v: "3", icon: AlertTriangle },
          ].map((s, i) => (
            <div key={i} className="bg-white rounded-xl p-3 shadow-sm">
              <div className="flex items-center justify-between mb-1">
                <p className="text-[9px] text-[#8D6E63]">{s.l}</p>
                <s.icon size={12} style={{ color: ORANGE }} />
              </div>
              <p className="text-sm font-bold text-[#4E342E] font-[Poppins]">{s.v}</p>
            </div>
          ))}
        </div>
        <div className="bg-white rounded-xl p-3 shadow-sm">
          <p className="text-xs font-semibold text-[#1A1210] mb-2 font-[Poppins]">Weekly Revenue</p>
          <ResponsiveContainer width="100%" height={100}>
            <AreaChart data={revenueData}>
              <defs><linearGradient id="mob" x1="0" y1="0" x2="0" y2="1"><stop offset="5%" stopColor={ORANGE} stopOpacity={0.3} /><stop offset="95%" stopColor={ORANGE} stopOpacity={0} /></linearGradient></defs>
              <Area type="monotone" dataKey="revenue" stroke={ORANGE} strokeWidth={2} fill="url(#mob)" />
            </AreaChart>
          </ResponsiveContainer>
        </div>
        <div className="bg-white rounded-xl p-3 shadow-sm">
          <p className="text-xs font-semibold text-[#1A1210] mb-2 font-[Poppins]">Low Stock Alerts</p>
          {inventory.filter(i => i.stock < i.min).map(item => (
            <div key={item.id} className="flex items-center gap-2 py-1.5 border-b border-[rgba(78,52,46,0.06)] last:border-0">
              <AlertTriangle size={11} className="text-red-500 flex-shrink-0" />
              <p className="text-[10px] text-[#1A1210] flex-1">{item.item}</p>
              <span className="text-[9px] font-bold text-red-500">{item.stock}{item.unit}</span>
            </div>
          ))}
        </div>
      </div>
    </div>
  );
}

function MobileScreensSection() {
  return (
    <div className="p-8 space-y-12" style={{ background: "#F3EDE9", minHeight: "100vh" }}>
      <div>
        <h1 className="text-2xl font-bold text-[#1A1210] font-[Poppins]">Mobile App Mockups</h1>
        <p className="text-[#8D6E63] mt-1">Customer, Staff & Manager apps — 375px phone frames</p>
      </div>

      <div>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-6">Customer App</h2>
        <div className="flex flex-wrap gap-6">
          <PhoneFrame label="Splash Screen"><MobileSplash /></PhoneFrame>
          <PhoneFrame label="Login"><MobileLogin /></PhoneFrame>
          <PhoneFrame label="Home"><MobileHome /></PhoneFrame>
          <PhoneFrame label="Menu"><MobileMenu /></PhoneFrame>
          <PhoneFrame label="Cart"><MobileCart /></PhoneFrame>
          <PhoneFrame label="Loyalty"><MobileLoyalty /></PhoneFrame>
        </div>
      </div>

      <div>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-6">Staff App</h2>
        <div className="flex flex-wrap gap-6">
          <PhoneFrame label="Order Queue"><StaffMobileOrders /></PhoneFrame>
          <PhoneFrame label="Inventory">
            <div className="h-full overflow-auto p-3" style={{ background: DARK_BROWN }}>
              <MobileStatusBar dark />
              <p className="text-white font-bold text-sm px-1 mb-3 font-[Poppins]">Inventory</p>
              <div className="space-y-2">
                {inventory.map(item => (
                  <div key={item.id} className={`rounded-xl p-3 ${item.stock < item.min ? "bg-red-50" : "bg-white"}`}>
                    <div className="flex justify-between items-start">
                      <p className="text-[10px] font-semibold text-[#1A1210]">{item.item}</p>
                      {item.stock < item.min && <AlertTriangle size={10} className="text-red-500" />}
                    </div>
                    <p className="text-[9px] text-[#8D6E63] mt-0.5">{item.stock} {item.unit} · min {item.min}{item.unit}</p>
                  </div>
                ))}
              </div>
            </div>
          </PhoneFrame>
          <PhoneFrame label="Attendance">
            <div className="h-full flex flex-col" style={{ background: DARK_BROWN }}>
              <MobileStatusBar dark />
              <div className="flex-1 flex flex-col items-center justify-center px-6 gap-4">
                <div className="w-20 h-20 rounded-full border-4 border-orange-400 flex items-center justify-center">
                  <Clock size={32} style={{ color: ORANGE }} />
                </div>
                <p className="text-white text-3xl font-bold font-[Poppins]">09:24</p>
                <p className="text-[#D7CCC8] text-xs">Tuesday, 24 June 2025</p>
                <p className="text-[#D7CCC8] text-xs">Sipho Mokoena · Supervisor</p>
                <div className="flex gap-3 w-full mt-4">
                  <button className="flex-1 py-3 rounded-xl font-semibold text-white text-xs" style={{ background: GREEN }}>Clock In</button>
                  <button className="flex-1 py-3 rounded-xl font-semibold text-white text-xs" style={{ background: RED }}>Clock Out</button>
                </div>
              </div>
            </div>
          </PhoneFrame>
        </div>
      </div>

      <div>
        <h2 className="text-sm font-semibold text-[#8D6E63] uppercase tracking-wider mb-6">Manager App</h2>
        <div className="flex flex-wrap gap-6">
          <PhoneFrame label="Manager Dashboard"><ManagerMobileDash /></PhoneFrame>
          <PhoneFrame label="Staff Overview">
            <div className="h-full overflow-auto bg-[#FAFAFA]">
              <MobileStatusBar />
              <div className="px-3">
                <p className="font-bold text-sm text-[#1A1210] font-[Poppins] mb-3">Staff Overview</p>
                <div className="space-y-2">
                  {staff.map(s => (
                    <div key={s.id} className="flex items-center gap-3 bg-white rounded-xl p-3 shadow-sm">
                      <div className="w-8 h-8 rounded-full flex items-center justify-center text-white font-bold text-xs flex-shrink-0" style={{ background: BROWN }}>{s.name.charAt(0)}</div>
                      <div className="flex-1">
                        <p className="text-[10px] font-semibold text-[#1A1210]">{s.name}</p>
                        <p className="text-[9px] text-[#8D6E63]">{s.role} · {s.shift}</p>
                      </div>
                      <div className={`w-2 h-2 rounded-full ${s.status === "Active" ? "bg-green-500" : "bg-gray-300"}`} />
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </PhoneFrame>
          <PhoneFrame label="Event Overview">
            <div className="h-full overflow-auto bg-[#FAFAFA]">
              <MobileStatusBar />
              <div className="px-3">
                <p className="font-bold text-sm text-[#1A1210] font-[Poppins] mb-3">Events</p>
                <div className="space-y-3">
                  {events.map(e => (
                    <div key={e.id} className="bg-white rounded-xl overflow-hidden shadow-sm">
                      <img src={`https://images.unsplash.com/photo-${e.img}?w=300&h=100&fit=crop&auto=format`} alt={e.title} className="w-full h-20 object-cover" />
                      <div className="p-2.5">
                        <p className="text-[10px] font-semibold text-[#1A1210]">{e.title}</p>
                        <div className="flex justify-between mt-1">
                          <p className="text-[9px] text-[#8D6E63]">{e.date} · {e.time}</p>
                          <p className="text-[9px] font-bold text-[#4E342E]">{e.price}</p>
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              </div>
            </div>
          </PhoneFrame>
        </div>
      </div>
    </div>
  );
}

// ─── MAIN APP SHELL ───────────────────────────────────────────────────────────
type Section = "website" | "staff" | "manager" | "mobile" | "design";
type WebScreen = "home" | "menu" | "cart" | "login" | "register" | "dashboard" | "events";
type StaffScreen = "ops" | "pos" | "orders" | "inventory" | "attendance" | "schedule";
type MgrScreen = "mgrdash" | "staffmgmt" | "suppliers" | "eventmgmt" | "reports" | "analytics";

export default function App() {
  const [section, setSection] = useState<Section>("website");
  const [webScreen, setWebScreen] = useState<WebScreen>("home");
  const [staffScreen, setStaffScreen] = useState<StaffScreen>("ops");
  const [mgrScreen, setMgrScreen] = useState<MgrScreen>("mgrdash");

  const sections: { id: Section; label: string }[] = [
    { id: "design", label: "Design System" },
    { id: "website", label: "Customer Website" },
    { id: "staff", label: "Staff Portal" },
    { id: "manager", label: "Manager Dashboard" },
    { id: "mobile", label: "Mobile Apps" },
  ];

  const webScreens: { id: WebScreen; label: string }[] = [
    { id: "home", label: "Home" },
    { id: "menu", label: "Menu" },
    { id: "cart", label: "Cart" },
    { id: "login", label: "Login" },
    { id: "register", label: "Register" },
    { id: "dashboard", label: "Customer Dashboard" },
    { id: "events", label: "Events" },
  ];

  return (
    <div className="min-h-screen flex flex-col font-[Inter]">
      {/* Top Section Nav */}
      <div className="flex items-center gap-1 px-4 py-2 border-b border-[rgba(78,52,46,0.15)] bg-white sticky top-0 z-50 shadow-sm">
        <div className="flex items-center gap-2 mr-4">
          <Coffee size={18} style={{ color: ORANGE }} />
          <span className="font-bold text-sm font-[Poppins]" style={{ color: BROWN }}>Skinner Cafe UI</span>
        </div>
        {sections.map(s => (
          <button
            key={s.id}
            onClick={() => setSection(s.id)}
            className="px-4 py-1.5 rounded-lg text-sm font-medium transition-all"
            style={section === s.id ? { background: BROWN, color: "white" } : { color: MUTED }}
          >
            {s.label}
          </button>
        ))}
        <div className="ml-auto flex items-center gap-2">
          <span className="text-xs text-[#8D6E63] bg-[#F3EDE9] px-2.5 py-1 rounded-full">
            {section === "website" ? `Screen: ${webScreens.find(s => s.id === webScreen)?.label}` :
              section === "staff" ? `Screen: ${staffScreen}` :
              section === "manager" ? `Screen: ${mgrScreen}` :
              section === "mobile" ? "All Mobile Screens" :
              "Design System"}
          </span>
        </div>
      </div>

      <div className="flex flex-1 overflow-hidden">
        {/* Sub-navigation */}
        {section === "website" && (
          <aside className="w-48 flex-shrink-0 bg-[#F3EDE9] border-r border-[rgba(78,52,46,0.1)] py-4 px-2">
            <p className="text-[10px] font-semibold text-[#8D6E63] uppercase tracking-wider px-3 mb-2">Screens</p>
            {webScreens.map(s => (
              <button key={s.id} onClick={() => setWebScreen(s.id)} className={`w-full text-left px-3 py-2 rounded-lg text-sm font-medium mb-0.5 transition-all ${webScreen === s.id ? "bg-white text-[#4E342E] shadow-sm" : "text-[#8D6E63] hover:bg-white/50"}`}>
                {s.label}
              </button>
            ))}
          </aside>
        )}

        {/* Main content */}
        <main className="flex-1 overflow-auto" style={{ background: section === "staff" || section === "manager" ? BG : "transparent" }}>
          {section === "design" && <DesignSystemScreen />}

          {section === "website" && (
            <>
              {webScreen === "home" && <HomeScreen />}
              {webScreen === "menu" && <MenuScreen />}
              {webScreen === "cart" && <CartScreen />}
              {webScreen === "login" && <LoginScreen />}
              {webScreen === "register" && <RegisterScreen />}
              {webScreen === "dashboard" && <CustomerDashboard />}
              {webScreen === "events" && <EventsScreen />}
            </>
          )}

          {section === "staff" && (
            <div className="flex h-full">
              <StaffSidebar active={staffScreen} setActive={(s: StaffScreen) => setStaffScreen(s)} />
              <div className="flex-1 flex flex-col overflow-hidden">
                <OpsTopbar />
                <div className="flex-1 overflow-auto">
                  {staffScreen === "ops" && <OpsDashboardContent />}
                  {staffScreen === "pos" && <POSScreen />}
                  {staffScreen === "orders" && <OrderQueueScreen />}
                  {staffScreen === "inventory" && <InventoryScreen />}
                  {staffScreen === "attendance" && <AttendanceScreen />}
                  {staffScreen === "schedule" && <ScheduleScreen />}
                </div>
              </div>
            </div>
          )}

          {section === "manager" && (
            <div className="flex h-full">
              <ManagerSidebar active={mgrScreen} setActive={(s: MgrScreen) => setMgrScreen(s)} />
              <div className="flex-1 overflow-auto">
                {mgrScreen === "mgrdash" && <ManagerDashContent />}
                {mgrScreen === "staffmgmt" && <StaffMgmtScreen />}
                {mgrScreen === "suppliers" && <SupplierScreen />}
                {mgrScreen === "eventmgmt" && <EventMgmtScreen />}
                {mgrScreen === "reports" && <ReportsScreen />}
                {mgrScreen === "analytics" && <AnalyticsScreen />}
              </div>
            </div>
          )}

          {section === "mobile" && <MobileScreensSection />}
        </main>
      </div>
    </div>
  );
}
