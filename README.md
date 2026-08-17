# VideoClip-Cost-Agent
Agent that decides the cheapest way to find and cut content or moments from a video clip,
(e.g. a spoken word, or a scene change) and reports back the cost incurred, given a
budget in pounds.

It calls a (stubbed) speech-to-text service first, since it's cheaper, and only
escalates to a (stubbed) vision service when speech-to-text results are unreliable
and the budget allows it. All decisions are made dynamically based on confidence
scores and remaining budget - this is not a fixed sequence of calls.

## How to run it

Open the project in IntelliJ (or any Java 17+ environment with Maven) and run
`Main.java`. This launches a small desktop GUI - no external setup, API keys, or
network access needed beyond Maven fetching the one video-parsing dependency on
first build.

The window lets you:
- Pick one of three preset scenarios and click **Run Agent** to see a guaranteed,
  reproducible decision trail and cost breakdown for each.
- Or select **"4. Custom scenario"** to enter your own target description, budget,
  and either a manually-typed video length or a real `.mp4`/`.mov` file (its actual
  duration is read and used).

## How the agent decides

1. Always starts with a **narrow, cheap speech-to-text search** (±10s around the
   best available guess of where the target is).
2. If it returns a confident match (confidence ≥ 80%), the agent stops immediately -
   this is the cheapest possible outcome.
3. If not confident:
   - If confidence was **very low** (<30%), speech-to-text is judged unreliable
     for this case, so the agent escalates straight to vision (if affordable).
   - If confidence was **moderate**, the agent retries speech-to-text with a
     **wider window** first, since it's cheaper than vision and may just need
     more range.
4. If a retry is also inconclusive, the agent escalates to vision as a last resort
   (if affordable).
5. At every branch, the agent checks `CostTracker.canAfford(...)` before
   committing to a path. If the preferred option isn't affordable, it falls back
   to a cheaper one, or stops and honestly reports that it gave up, with reasoning.

Speech-to-text costs a base fee plus a per-second-of-window charge (so a wider
search genuinely costs more), and vision costs per frame checked - both queried
live from the services themselves via `estimateCost(...)`, so the agent's budget
checks can never drift out of sync with actual pricing.

## The three preset scenarios

| Scenario | Budget | What happens | Cost |
|---|---|---|---|
| **Tight Budget - Easy Find** | £0.10 | Narrow search finds a confident match immediately | £0.05 |
| **Generous Budget - Retry Needed** | £2.00 | Narrow search misses, wider retry succeeds | £0.18 |
| **Ambiguous Target - Budget Runs Out** | £0.40 | Narrow and wide retries are both inconclusive, escalates to vision, vision is also inconclusive, agent gives up honestly | £0.38 |

These run with fixed random seeds, so the outcomes above are reproducible every time.

## Custom scenarios

The fourth option lets you try the agent on an unscripted case:
- Type your own target description, rough guess, and budget.
- Either type a video length manually, or click **"Select video file..."** to pick
  a `.mp4`/`.mov` file - its duration is read from the file and used.
- The "correct" position of the target is generated randomly within the video's
  length and is never shown anywhere in the app - the agent has to find it the
  same way a real speech-to-text/vision service would, with no shortcuts.
- Because this path uses unseeded randomness, running it multiple times with the
  same inputs will produce different outcomes each time.

**Known limitation:** video duration reading uses the `mp4parser` library, which
doesn't support every possible MP4/MOV internal structure (some phone-exported or
re-encoded files use a layout it can't parse). When this happens, the app shows a
clear error and falls back to manual entry rather than failing silently.

## Design notes

- **Stubbed services, not real APIs.** Per the brief, `SpeechToTextService` and
  `VisionService` simulate realistic latency, cost, and probabilistic accuracy,
  rather than calling real (paid) APIs.
- **Seeded randomness for presets, unseeded for custom runs.** The three presets
  use fixed seeds so their outcomes are guaranteed and repeatable for demo
  purposes; the custom scenario path uses genuinely random seeds each run.
- **Fresh service instances per run**, so no scenario's outcome depends on what
  ran before it.
- **GUI output builds a list of report lines** (`AgentResult.getReportLines()`)
  rather than printing directly, so the same report data can be revealed
  line-by-line in the GUI via a non-blocking `javax.swing.Timer`, without freezing
  the window.

## What I'd do next with more time

- Replace `mp4parser` with a more robust reading approach (e.g. shelling
  out to `ffprobe`) to support a range of real video files reliably.
- Make the decision policy's thresholds (confidence cutoffs, window sizes, frame
  counts) configurable rather than hardcoded, so different cost/reliability
  trade-offs could be tuned per platform or client.
- Add unit tests around `Agent`'s branching logic directly (currently verified
  by hand via the GUI and simulation scripts during development).
- Let the agent reason about partial vision escalations (e.g. fewer frames) when
  budget is tight but non-zero, rather than the current all-or-nothing affordability
  check against a fixed frame count.
- Show the (currently hidden) ground truth position after a custom run completes,
  as an optional "reveal answer" toggle, purely for demo/debugging purposes.
