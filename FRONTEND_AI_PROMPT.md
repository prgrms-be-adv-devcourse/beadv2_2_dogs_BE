## Frontend Task Prompt (for Generative AI)

Goal:
- Build the product detail page + review UI end-to-end.
- Connect APIs, generate components, and wire data flow.
- Keep output simple and easy for a non-expert to understand.

Base URLs:
- Buyer service: `http://localhost:8082/api/v1`
- Support service (reviews): `http://localhost:8089/api/v1`

Response shape:
- Every API returns `ResponseDto<T>`: `{ status: number, data: T, message: string | null }`.
- Use the `data` field as the actual payload.

Key APIs:
1) Product detail (buyer)
- `GET /products/{productId}`
- Response `data` contains fields like:
  - `productName`, `description`, `price`, `imageUrls`
  - `positiveReviewSummary: string[]`
  - `negativeReviewSummary: string[]`

2) Product reviews list (support)
- `GET /products/{productId}/reviews`
- Query: `page`, `size`, `sort` (standard Spring pageable)

3) Review create (support)
- `POST /products/{productId}/reviews` (multipart/form-data)
- Headers: `X-User-Id`
- Parts:
  - `data` (JSON):
    - `orderItemId` (UUID)
    - `rating` (1~5)
    - `reviewVisibility` (`PUBLIC` or `PRIVATE`)
    - `content` (string)
  - `images` (0..n files)

4) Review detail (support)
- `GET /reviews/{reviewId}`
- Header: `X-User-Id`

5) Review update (support)
- `PUT /reviews/{reviewId}` (multipart/form-data)
- Headers: `X-User-Id`
- Parts:
  - `data` (JSON):
    - `rating` (1~5)
    - `reviewVisibility` (`PUBLIC` or `PRIVATE`)
    - `content`
    - `imageUpdateMode` (`KEEP`, `REPLACE`, `CLEAR`)
  - `images` (optional)

6) Review delete (support)
- `DELETE /reviews/{reviewId}`
- Header: `X-User-Id`

UI Flow (simple explanation):
- Load product detail first (name, price, images).
- Show review summaries (positive/negative lists) under the product info.
- Load paged reviews list for that product.
- Provide a form to create a review (rating, content, visibility, images).
- Provide edit/delete actions per review (if user is owner).

Implementation notes:
- Build a small API client wrapper that:
  - adds `X-User-Id` header when required.
  - unwraps `ResponseDto.data`.
- Use FormData for create/update review.
- Keep components small: `ProductDetail`, `ReviewSummary`, `ReviewList`, `ReviewForm`.
- Prefer `List<String>` summaries to render each line as a bullet.

Deliverables expected from the frontend AI:
- API client module
- React components for the flow above
- Basic styles for readability
- Clear comments or README-level usage notes
