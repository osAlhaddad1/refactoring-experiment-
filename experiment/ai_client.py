"""Talks to the AI model (Google Gemini), plus a mock client for testing the
pipeline without spending tokens.

The real client reads the API key from GEMINI_API_KEY and the model name from
GEMINI_MODEL. If either is missing it prints a clear error and stops.

Every client returns the same dict:
    {"text": <model output>, "input_tokens": int, "output_tokens": int, "latency_ms": int}
"""

import os
import sys
import time

GEMINI_URL = "https://generativelanguage.googleapis.com/v1beta/models/%s:generateContent"


def call_gemini(prompt):
    """Calls the Gemini generateContent endpoint at temperature 0."""
    import requests  # imported here so the mock works even without requests installed

    api_key = os.environ.get("GEMINI_API_KEY")
    model = os.environ.get("GEMINI_MODEL")
    if not api_key or not model:
        print("ERROR: set the GEMINI_API_KEY and GEMINI_MODEL environment variables.")
        sys.exit(1)

    url = GEMINI_URL % model
    body = {
        "contents": [{"parts": [{"text": prompt}]}],
        "generationConfig": {"temperature": 0},
    }

    # send the request; retry on rate limits (429) and transient network errors
    attempt = 0
    while True:
        attempt = attempt + 1
        start = time.time()
        try:
            response = requests.post(url, params={"key": api_key}, json=body, timeout=300)
        except requests.exceptions.RequestException as error:
            if attempt <= 5:
                print("    (network problem calling Gemini: %s; waiting 30s then retrying)"
                      % error.__class__.__name__)
                time.sleep(30)
                continue
            raise
        latency_ms = int((time.time() - start) * 1000)
        if response.status_code == 429 and attempt <= 5:
            print("    (API rate limit hit; waiting 60s then retrying)")
            time.sleep(60)
            continue
        break
    response.raise_for_status()
    data = response.json()

    text = ""
    candidates = data.get("candidates", [])
    if candidates:
        parts = candidates[0].get("content", {}).get("parts", [])
        text = "".join(part.get("text", "") for part in parts)

    usage = data.get("usageMetadata", {})
    return {
        "text": text,
        "input_tokens": usage.get("promptTokenCount", 0),
        "output_tokens": usage.get("candidatesTokenCount", 0),
        "latency_ms": latency_ms,
    }


def call_mock(prompt, mock_file):
    """Returns a hardcoded JSON response read from mock_file, with fake token
    counts (so the pipeline can be tested without calling the real model)."""
    start = time.time()
    with open(mock_file, encoding="utf-8") as f:
        text = f.read()
    latency_ms = int((time.time() - start) * 1000)
    return {
        "text": text,
        "input_tokens": len(prompt) // 4,    # rough, fake
        "output_tokens": len(text) // 4,     # rough, fake
        "latency_ms": latency_ms,
    }
