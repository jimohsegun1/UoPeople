# Unit 2 Assignment Activity: Travel Blog Website

**Course:** CS 2205-01: Web Programming 1 (AY2027-T2)  
**Student:** Jeremiah Segun Jimoh  
**Institution:** University of the People  
**Submission Package:** `CS2205_Unit2_Travel_Blog_Jeremiah_Jimoh`  

---

## Deliverables & Architecture

This unit consists of two linked HTML5 pages with self-contained CSS3 in their `<head>` elements (per assignment instructions) and local artwork:

1. **`index.html` (Homepage - Exercises 1a & 2a)**
   - **4 Color-Coded Number Blocks:** Quick jump shortcuts (1: Europe `#1E5B88`, 2: Asia `#1E7A62`, 3: America `#C85A32`, 4: Africa `#6B3E7A`).
   - **Interactive Hover & Focus:** 0.28s transform lift (`translateY(-7px) scale(1.04)`), glowing gold outline, and elevated drop shadow.
   - **Visual Hierarchy in Nested Lists:**
     - Level 1: Continent headings colored according to each block.
     - Level 2: Ordered country lists (`<ol>`) styled with rich amber subheadings (`#B45309`).
     - Level 3: Unordered city sublists (`<ul>`) rendered inside softly shaded slate containers (`#0F4C5C`).
   - **Target Highlighting:** `:target` pseudo-class applies a warm amber halo (`#F59E0B`) to orient the visitor upon clicking any shortcut.

2. **`travel-stories.html` (Travel Stories - Exercises 1b & 2b)**
   - **Two-Column Destination Table:**
     - Left column: Destination name, country, and brief overview.
     - Right column: Clickable landmark artwork wrapped in anchor tags jumping to detailed stories.
   - **Table CSS Styling:** Deep cobalt outer border (`#1E5B88`), bold high-contrast headers with coral accent (`#E07A5F`), cyan cell borders (`#3B82A6`), alternating row striping (`#F0F7FC` / `#FFF7F0`), and generous cell padding (`20px 24px`).
   - **Image Hover Transition:** Smooth opacity fade (`opacity: 0.62; transform: scale(1.03)`) providing tactile visual confirmation of clickability.
   - **Target Story Cards:** Detailed articles at bottom of page with `:target` highlighting and `Back to Table` return links.

3. **Global Navigation Bar Enhancement**
   - Translucent frosted glass capsule (`rgba(255, 255, 255, 0.14)` with subtle white border).
   - Crisp white active badge (`#FFFFFF` with `#16324F` text).
   - Distinctive golden yellow hover micro-interaction (`#FFC700` background, `#16324F` text, `translateY(-2px)`, and warm amber glow) ensuring high contrast and zero ambiguity.

4. **`images/`**
   - `eiffel-tower.png`: Paris, France
   - `colosseum.png`: Rome, Italy
   - `statue-of-liberty.png`: New York, USA
