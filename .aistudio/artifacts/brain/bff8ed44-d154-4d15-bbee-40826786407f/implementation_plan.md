# Global Layout, Spacing, and Bottom Sheet Standardization Plan

Auditing and refactoring all main screens, details views, dialogs, and Modal Bottom Sheets across Hisab to enforce a strict, consistent fintech design system with uniform 16dp margins, standardized headers, input containers, card radii, and action button dimensions.

## User Review & Critical Decisions

> [!IMPORTANT]
> The following parameters were confirmed during the design preference review and will serve as strict constants across the refactor:

- **Modal Bottom Sheets Corner Radius**: `RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)` on all bottom sheet containers.
- **Dialogs Corner Radius**: `RoundedCornerShape(20.dp)` on all custom `Dialog` and `AlertDialog` card containers.
- **General Content Cards Radius & Border**: `RoundedCornerShape(16.dp)` with `BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant)` across dashboards, list items, statistics, and informational cards.
- **Primary Action Buttons**: Full-width buttons spanning edge-to-edge within standard 16dp horizontal margins, fixed height of `52.dp`, `RoundedCornerShape(12.dp)`, and high-contrast text color (`#FFFFFF`).
- **Input Fields & Search Containers**: Surface background `#161F30` in dark mode / `LightSurfaceVariant` in light mode, with a 1dp border and `12.dp` corner radius.
- **Scrollable Chip Rows**: Standardized content padding with `PaddingValues(start = 16.dp, end = 16.dp)` or `PaddingValues(end = 16.dp)` to prevent edge truncation.

---

## 1. Overview & Core Concept

### What It Does
This refactoring establishes a centralized spatial and layout standard across the entire Hisab application. It replaces fragmented padding values (e.g., 20dp, 14dp, 22dp, 8dp) with a unified 16dp grid system, aligns all Modal Bottom Sheet structures (drag handle + 18sp title + close icon + safe bottom inset padding), standardizes input fields to unified dark/light surfaces with 12dp radii, and ensures every primary action button provides a consistent 52dp touch target.

### Target Audience & Persona
Everyday users managing personal finances, shop credits, and loans who demand a predictable, polished, high-contrast fintech interface where controls feel uniform and responsive on any Android screen size.

### Key Value
- **Visual Consistency**: Predictable screen rhythm, card borders, and corner curves create an uncluttered, premium fintech aesthetic.
- **System Inset Safety**: Modal sheets and screens will reliably handle virtual navigation bars and software keyboards using `imePadding()`, `navigationBarsPadding()`, and 16dp bottom safety buffers.
- **Accessibility & Touch Precision**: Strict 52dp button heights and 48dp minimum touch targets across all sheets, forms, and dialogs.

---

## 2. User Experience & Visual Design

### Key User Flows

1. **Transaction Entry & Modification (Bottom Sheet & Full Screen)**:
   - User opens *New Transaction* or *Edit Transaction*.
   - Sheet presents a standardized top bar: centered drag handle, bold 18sp title on the left, and a 48dp circular close icon (`✕`) on the right.
   - 16dp horizontal screen edge padding wraps all form sections.
   - Quick amounts and account chips scroll horizontally with 16dp end margins so the last chip is fully readable.
   - Large 52dp action button sits firmly at the bottom with high-contrast white text.

2. **Filtering & Range Selection**:
   - Filter bottom sheet presents the unified header layout with title and close icon.
   - Period, transaction type, and account chip groups use consistent 16dp vertical spacing and matching 16dp outer padding.
   - Clear Reset and Apply actions styled uniformly.

3. **Debts, Shop Credits, Budgets & Accounts Dialogs**:
   - All creation modals (Loans, Shop credits, Accounts, Budgets, Savings goals) follow the 24dp top-radius sheet standard or 20dp dialog standard.
   - Form fields use identical 12dp corner radii, 1dp subtle outline borders, and dark `#161F30` / light variant surface fills.

### Visual Identity & Theme Tokens

| Token | Specification | Application |
|---|---|---|
| **Screen Edge Horizontal Padding** | `16.dp` | All root scaffold contents, lists, sheets, and forms |
| **Major Section Gap** | `16.dp` vertical spacing | Gaps between form sections, card groups, and summaries |
| **Bottom Sheet Top Radius** | `24.dp` | `topStart = 24.dp, topEnd = 24.dp` |
| **Dialog Container Radius** | `20.dp` | All `Dialog` card containers |
| **Card Corner Radius** | `16.dp` | Dashboard balance cards, debt cards, list item cards |
| **Card Border** | `1.dp` | `outlineVariant` (light) / `Color(0xFF1E3A4A)` (dark) |
| **Input Field Radius** | `12.dp` | Amount, notes, date/time cards, search bars |
| **Input Container Color** | `Color(0xFF161F30)` (dark) | Consistent background across all text fields & search |
| **Action Button Height** | `52.dp` | Primary Save, Apply, and Confirm buttons |
| **Action Button Content Color** | `Color(0xFFFFFFFF)` | High-contrast white typography |
| **Horizontal Chip Padding** | `PaddingValues(end = 16.dp)` | Prevents cut-off of the rightmost chip |

---

## 3. Key Product Decisions & Trade-Offs

### Decision 1: Shared Bottom Sheet Header & Container Pattern
- **Chosen Approach**: Standardize the top header layout in all bottom sheets (`AddTransactionBottomSheet`, `TransactionFilterBottomSheet`, `MonthYearPickerBottomSheet`, and sheets in `DebtsScreen` and `BudgetsAndGoalsScreen`) to consistently display:
  1. Top `BottomSheetDefaults.DragHandle`.
  2. A dedicated header `Row(modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp))` with 18sp Medium/Bold title and explicit `IconButton` with `Icons.Default.Close`.
  3. `imePadding() + navigationBarsPadding() + Modifier.padding(bottom = 16.dp)` on inner scrollable content.
- **Why**: Eliminates layout jumping between different sheets and prevents floating keyboard or 3-button navigation overlaps.
- **Alternatives Considered**: Material 3 default header without close button was rejected because users expect an explicit `✕` tap target in fintech modals.

### Decision 2: Card & Input Container Standardization
- **Chosen Approach**: Standardize all cards across `DashboardScreen`, `AccountsScreen`, `DebtsScreen`, `BudgetsAndGoalsScreen`, and `RecurringScreen` to `16.dp` corner radius and `1.dp` border. Input fields will share `12.dp` corner radius and identical dark `#161F30` container colors.
- **Why**: Distinct card radius (`16.dp`) vs input radius (`12.dp`) establishes clear visual hierarchy between grouping surfaces and interactive form elements.

### Decision 3: Action Button Uniformity
- **Chosen Approach**: Full-width primary buttons with `Modifier.fillMaxWidth().height(52.dp)` and `RoundedCornerShape(12.dp)` across bottom sheets and detail forms.
- **Why**: Meets accessibility touch target requirements (exceeding 48dp) and maintains tactile prominence on mobile screens.

---

## 4. Technical Architecture & Component Hierarchy

```
┌────────────────────────────────────────────────────────────────────────┐
│                        Hisab Design System Standard                     │
└────────────────────────────────────┬───────────────────────────────────┘
                                     │
           ┌─────────────────────────┴────────────────────────┐
           ▼                                                  ▼
┌──────────────────────────────────────┐   ┌─────────────────────────────────────┐
│       Modal Bottom Sheets            │   │           Screens & Dialogs         │
│  - Radius: 24dp top corners          │   │  - Screen Margin: 16dp horizontal   │
│  - Drag Handle at top                │   │  - Section Spacing: 16dp vertical   │
│  - Header: 18sp Title + '✕' Close    │   │  - Dialogs: 20dp corner radius      │
│  - Insets: ime + navBars + 16dp      │   │  - Cards: 16dp radius + 1dp border  │
│  - Primary Button: 52dp full width   │   │  - Inputs: 12dp radius, #161F30 bg  │
└──────────────────┬───────────────────┘   └──────────────────┬──────────────────┘
                   │                                          │
                   ▼                                          ▼
┌──────────────────────────────────────┐   ┌─────────────────────────────────────┐
│  Audited Bottom Sheets:              │   │  Audited Screens & Dialogs:         │
│  • AddTransactionBottomSheet         │   │  • DashboardScreen & BalanceCard    │
│  • TransactionFilterBottomSheet      │   │  • TransactionsScreen & Search Row  │
│  • MonthYearPickerBottomSheet        │   │  • AddEditTransactionScreen         │
│  • DebtsScreen sheets (Add loan,     │   │  • DebtsScreen & Contacts           │
│    Record payment, Shop credit)      │   │  • BudgetsAndGoalsScreen            │
│  • BudgetsAndGoalsScreen sheets      │   │  • AccountsScreen & AccountDialog   │
│    (Add budget, Savings goal)        │   │  • DeleteConfirmationDialog         │
│                                      │   │  • UpdateDialog & PinLockDialog     │
│                                      │   │  • RecurringScreen & SettingsScreen │
└──────────────────────────────────────┘   └─────────────────────────────────────┘
```

### Component & State Mapping

1. **`AddTransactionBottomSheet`**:
   - Update header horizontal padding to `16.dp`.
   - Update title style to `18.sp` with bold weight, accompanied by explicit `Close` action icon.
   - Enforce `imePadding()`, `navigationBarsPadding()`, and `16.dp` safety bottom margin.
   - Set Save button height to `52.dp` with `16.dp` side margins.
   - Ensure horizontal chip lists have `PaddingValues(end = 16.dp)`.

2. **`TransactionFilterBottomSheet` & `MonthYearPickerBottomSheet`**:
   - Align drag handle, 18sp title, and explicit close button.
   - Enforce 16dp edge padding and 52dp full-width apply action button.
   - Apply `imePadding()`, `navigationBarsPadding()`, and 16dp bottom spacing.

3. **`DashboardScreen` & `DashboardBalanceCard`**:
   - Standardize all dashboard cards (Balance hero card, Lending/shop summary card, Update cards, and recent transactions) to `16.dp` corner radius and `1.dp` border.
   - Enforce consistent 16dp horizontal edge margins and 16dp vertical section spacing.

4. **`TransactionsScreen` & `AddEditTransactionScreen`**:
   - Standardize search input container with `12.dp` radius, 1dp border, and `#161F30` dark background.
   - Standardize active filter chip row and account selector chip rows with `PaddingValues(end = 16.dp)`.
   - Align form field containers, amount input, date picker, and note input to 12dp radius and 16dp section spacing.
   - Primary Save button set to full-width `52.dp` height with white text.

5. **`DebtsScreen`, `BudgetsAndGoalsScreen`, `AccountsScreen`, `RecurringScreen`**:
   - Refactor modal bottom sheets to the standardized 24dp top-radius container with drag handle and 18sp title + close icon.
   - Refactor dialogs (`DeleteConfirmationDialog`, `AddEditAccountDialog`, `AddRecurringRuleDialog`, `CategoryDrilldownDialog`, `UpdateDialog`) to 20dp corner radius and 1dp border.
   - Refactor all content and list item cards to 16dp corner radius and 1dp border.
