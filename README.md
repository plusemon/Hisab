# Hisab - Smart Expense & Financial Tracker

**Hisab** is a comprehensive, feature-rich Android financial management application built with **Kotlin** and **Jetpack Compose**. It is designed to help users track their daily income, expenses, multi-account balances, budgets, savings goals, loans/debts, shop credit, and recurring transactions with ease and local privacy.

---

## ✨ Key Features

- **Dashboard & Analytics**: Real-time overview of net worth, monthly cash flow, recent transactions, budget utilization, and quick summary cards.
- **Multi-Account Management**: Track cash, bank accounts, mobile wallets (bKash/Nagad/Rocket style or customized), and credit cards in one place.
- **Transaction Tracking**: Add, edit, categorize, and filter income and expense transactions with custom categories, icons, and notes.
- **Budgets & Savings Goals**: Set monthly or category-specific spending budgets and track progress toward financial savings goals.
- **Loan & Debt Tracking**: Manage lent/borrowed money, track repayments, and monitor shop credit/purchases.
- **Recurring Transactions**: Automate recurring bills, subscriptions, or salaries with flexible frequency rules.
- **Reports & Visualizations**: Detailed financial charts and reports to analyze spending habits over weekly, monthly, and yearly intervals.
- **Data Export & Import**: Backup and restore financial data seamlessly via CSV export/import.
- **Security & Privacy**: Offline-first architecture powered by **Room Database** ensuring complete data privacy, with optional PIN lock protection.

---

## 🛠️ Tech Stack & Architecture

- **UI Framework**: [Jetpack Compose](https://developer.android.com/jetpack/compose) with Material Design 3 (M3).
- **Architecture**: MVVM (Model-View-ViewModel) with Clean Architecture principles, Kotlin Coroutines, and Flow for reactive state management.
- **Local Persistence**: [Room Database](https://developer.android.com/training/data-storage/room) with KSP.
- **Navigation**: Navigation Compose with type-safe routing.
- **Language**: 100% Kotlin.

---

## 📂 Project Structure

```
/app
├── src/main/java/com/plusemon/hisab/
│   ├── data/
│   │   ├── local/          # Room DAOs and AppDatabase
│   │   ├── model/          # Entity data classes
│   │   └── repository/     # Repositories (HisabRepository, AuthRepository)
│   ├── domain/
│   │   └── util/           # CSV importer/exporter, Formatters, Localization
│   └── ui/
│       ├── components/     # Reusable Compose components (TopBar, BottomNav, Badges, etc.)
│       ├── navigation/     # NavRoutes definitions
│       ├── screens/        # Feature screens (Dashboard, Accounts, Budgets, Debts, Reports, Settings, Transactions, etc.)
│       ├── theme/          # M3 Color, Theme, and Typography
│       └── viewmodel/      # HisabViewModel
└── src/main/res/           # Android resources (drawables, strings, themes)
```

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug or newer / IntelliJ IDEA with Android plugin.
- Android SDK (compileSdk & targetSdk matching project configuration).
- Gradle (Kotlin DSL).

### Building & Running
1. Clone the repository or open the project in Android Studio.
2. Ensure the Android SDK is properly configured.
3. Build and run the app on an Android emulator or physical device using:
   ```bash
   gradle :app:assembleDebug
   ```

---

## 📄 License

This project is built for personal and educational use. Feel free to fork and customize for your financial tracking needs!
