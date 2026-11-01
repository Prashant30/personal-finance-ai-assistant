# Demo Script

Use these questions for a repeatable live demonstration. The values below come from the committed fictional CSV and should appear in the API evidence regardless of the model's wording.

| Question | Expected evidence |
|---|---|
| How much did I spend on groceries in October 2026? | `USD 201.05` |
| What were my top three merchants in October 2026? | Oak Street Apartments `1650.00`, Fresh Market `201.05`, City Electric `78.50` |
| Show my October 2026 spending by category. | Housing is highest at `1650.00`; total across categories is `2257.53` |
| Compare my total spending in September and October 2026. | September `2237.82`, October `2257.53`, difference `19.71` |

Verification rule: treat the `evidence` array as authoritative. Model prose is presentation, not the source of financial calculations.

