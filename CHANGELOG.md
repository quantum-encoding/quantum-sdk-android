# Changelog

Releases before 0.3.0 were tracked in git history only; see `git log`.

## 0.3.0

The reasoning state a tool loop has to hand back, and the cache key that keeps a
conversation on one shard.

### Added
- `ChatRequest.promptCacheKey` — any stable string the client keeps per
  conversation. The gateway hashes it with the caller's identity and forwards it
  as OpenAI/xAI `prompt_cache_key` (or `x-grok-conv-id` on the xAI
  chat-completions lane), so every turn of one conversation lands on the same
  warm provider cache shard. `null` = derived from the caller's identity alone,
  which puts all of that user's conversations on one shard. Generate one per
  conversation object and reuse it on every turn.

  `POST /qai/v1/chat` only: the session endpoint derives its key from the
  session ID and ignores a client-supplied one, which is why
  `SessionChatRequest` has no such field.

- `ContentBlock.reasoning` (a `JsonElement`) and `ContentBlock.mintedBy`, on
  blocks whose type is the new `"reasoning"`. This is the provider's own
  reasoning item, verbatim and opaque. It arrives interleaved with the
  `tool_use` blocks and must be echoed back unchanged, **in the position it
  arrived in**, on the next turn's assistant message: its place among the tool
  calls is how the provider learns where the reasoning sat, and replaying it
  behind the call it reasoned about is a different conversation the provider
  rejects. Dropping it re-bills the reasoning tokens on every round of a tool
  loop.

  `mintedBy` names the model that produced the block; reasoning state is bound
  to its model and is never replayed to a different one.

  Distinct from the existing `"thinking"` block, which is the human-readable
  summary of the same turn. One is for the reader, one is for the wire.

- `StreamEvent.thoughtSignature`, populated from the new `thought_signature`
  SSE event the gateway sends just before `done` on a Gemini 3 stream that
  ended in text, and from the atomic `tool_use` event — which a streaming tool
  loop previously had no way to read.

- `SessionChatRequest.reasoningEffort`. The session handler validates the same
  tiers as `/qai/v1/chat` (`none`, `low`, `medium`, `high`, `xhigh`, `max`) and
  this SDK had no field to send them on, so the session lane was stuck on the
  provider default.

### Changed
- `ContentBlock.thoughtSignature` is documented on **text** blocks as well as
  `tool_use` blocks: Gemini 3 signs a turn that ends in text. The field already
  accepted it — this states the contract, it is not a shape change.
- `providerOptions` is documented as an open map, one JSON object per provider
  key plus the flat `region` entry. Newly documented:
  `provider_options.openai.reasoning_summary`
  (`auto` | `concise` | `detailed` | `none`), `.reasoning_mode`
  (`standard` | `pro`), `.verbosity` (`low` | `medium` | `high`),
  `.text_format` (`text` | `json_object`), and
  `provider_options.xai.native_files` (boolean). The type was already
  `JsonObject`, so unnamed keys always rode through.
- `ChatRequest.reasoningEffort` documents `max`, which the gateway has always
  validated on every lane.

Additive against an older gateway: the new fields are simply absent.
