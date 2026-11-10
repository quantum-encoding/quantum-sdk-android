# Changelog

Releases before 0.3.0 were tracked in git history only; see `git log`.

## Unreleased

Every path the client calls now matches a route the gateway serves. Methods
whose route was retired on 10 Nov 2026, or never existed, are gone: each one
could only get a 404. Collection search now reaches its real route and speaks
its wire format.

### Removed (breaking)
- `collectionsDocuments` and its type `CollectionDocument`. It called
  `GET /qai/v1/rag/collections/{id}/documents`, a route the gateway has never
  served.
- `computeBilling` (extension function) and its types `BillingRequest`,
  `BillingEntry`, `BillingResponse`. It called `POST /qai/v1/compute/billing`,
  a route the gateway has never served.
- `extractDocument`, `chunkDocument`, `processDocument`
  (`POST /qai/v1/documents/extract`, `/chunk`, `/process`) and their types:
  `DocumentRequest`, `DocumentResponse`, `ChunkRequest`, `ChunkResponse`,
  `DocumentChunk`, `ProcessRequest`, `ProcessResponse`, and the aliases
  `ChunkDocumentRequest`, `ChunkDocumentResponse`, `ProcessDocumentRequest`,
  `ProcessDocumentResponse`.
- `batchSubmit`, `batchSubmitJsonl`, `batchJobs`, `batchJob`
  (`POST /qai/v1/batch`, `POST /qai/v1/batch/jsonl`, `GET /qai/v1/batch/jobs`,
  `GET /qai/v1/batch/jobs/{id}`) and their types: `BatchJob`, `BatchJobInput`,
  `BatchSubmitRequest`, `BatchSubmitResponse`, `BatchJsonlResponse`,
  `BatchJobInfo`, `BatchJobsResponse`. HeyGen video batching
  (`videoBatchSubmit`, `videoBatchStatus`) is a separate route and stays.
- `searchAnswer` (`POST /qai/v1/search/answer`) and its types:
  `SearchAnswerRequest`, `SearchAnswerResponse`, `SearchAnswerMessage`,
  `SearchAnswerChoice`, `SearchAnswerCitation`, plus the unused
  `SearchMessage`. `webSearch` and `searchContext` stay.
- `videoStudio` (`POST /qai/v1/video/studio`) and its types:
  `VideoStudioRequest`, `StudioVideoRequest`, `StudioClip`.

`videoPhotoAvatar` stays: `PhotoAvatarRequest` is the talking-photo body
(`photo_base64` + `script`), the one shape `POST /qai/v1/video/photo-avatar`
still accepts.

### Fixed (breaking)
- `collectionsSearch` posted to `/qai/v1/rag/search/collections`, which
  answers 404. It now posts to `POST /qai/v1/rag/collections/search`.
- `CollectionSearchRequest` matches what that handler reads: `query`
  (now required), `collectionIds` (`collection_ids`; empty searches every
  collection the caller can read) and `maxChunks` (`max_chunks`, the gateway
  defaults to 10). `maxResults` serialised as `max_results`, which the
  gateway ignores, and is replaced by `maxChunks`. `mode` is removed: the
  gateway has no search modes and ignored it.
- `CollectionSearchResult` matches what the handler sends: `content`,
  `score`, `collection` (name), `collectionId`, `documentId`, `filename` and
  `isShared`. `fileId` and `metadata` are removed; the gateway never sent
  either.
- `collectionsList` and `collectionsSearch` threw a decoding error when there
  was nothing to return, because the gateway encodes an empty list as JSON
  `null`. Both now return an empty list.
- `videoDigitalTwin` sent a render-style body (`avatar_id`, `script`,
  `voice_id`, `aspect_ratio`) to `POST /qai/v1/video/digital-twin`. That
  route creates a twin from training footage and answers 400 without
  `video_url`. The method now takes `DigitalTwinCreateRequest` (`name`,
  `videoUrl`, optional `avatarGroupId`) and returns `DigitalTwinCreateResponse`
  (`name`, `consentUrl`, `model`, `groupId`, `status`, `consentStatus`,
  `avatarId`, `requestId`), the synchronous answer the route sends, instead
  of `AsyncJobResponse`. `DigitalTwinRequest` is removed.

### Docs
- README endpoint table drops the Documents, Batch and Scanner rows and the
  search-answer and studio mentions. This SDK never shipped scanner methods.

## 0.4.0

The TTS request catches up with the gateway: a house voice you do not have to
name, and prose steering for Gemini.

### Added
- `TtsRequest.instructions` — style direction: tone, pace, accent, character.
  On Gemini it is prepended to the prompt and is the main way to steer a read,
  since Gemini exposes no knobs for any of it. On OpenAI only `gpt-4o-mini-tts`
  honours it; `tts-1`/`tts-1-hd` reject the field and the gateway drops it.
- `TtsRequest.language` — BCP-47 tag (`en-GB`, `es-ES`, `auto`). Gemini detects
  the language on its own; set this to pin the pronunciation or accent family.
  Also drives xAI pronunciation, where an English default sounds robotic on
  other languages.
- `TtsRequest.sampleRate` and `.bitRate` — Hz and bits/sec, xAI only.
- `TtsRequest.voiceSettings` and the new `TtsVoiceSettings` (`stability`,
  `similarityBoost`, `style`, `useSpeakerBoost`). ElevenLabs only. Every field
  is nullable: an absent knob leaves the provider default alone, and 0.0
  stability is a real setting, so a zeroed object would silently retune the
  voice.
- `TtsRequest.speakers` and the new `TtsSpeaker` (`name`, `voice`) — Gemini
  two-voice dialogue. Each entry pairs a speaker label used in `text`
  ("Lacey: …") with the prebuilt voice that reads it. Exactly two; the gateway
  rejects any other count with a 400, and `voice` is then ignored.
- `Voice.category`, `.model` and `.description`, plus `VoicesResponse.requestId`.
  `GET /qai/v1/voices` has always sent these and this SDK dropped them, so a
  caller could not tell which model to pass back for a voice without
  hardcoding the provider mapping.

`TtsRequest.model` was already effectively optional — `HttpClient`'s encoder
sets `encodeDefaults = false`, so an empty model was already omitted rather
than sent as `""`. It is now documented as such.

### Fixed
- The README's Text-to-Speech example did not compile: it named a type
  `TTSRequest` that does not exist in this SDK (it is `TtsRequest`) and
  printed `audio.audioUrl`, a property `TtsResponse` has never had. The audio
  arrives inline as `audioBase64`; there is no URL to fetch.

### Docs
- README gains "Steering a Gemini voice", summarising the gateway's
  `docs/TTS_GUIDE.md`: `instructions` for tone/accent/pace, the inline audio
  tags (`[whispers]`, `[excited]`, …) that go inside `text`, two-speaker
  dialogue, the 30 Gemini prebuilt voices, and which fields are xAI- or
  ElevenLabs-only.

Additive against an older gateway: the new fields are simply absent.

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
