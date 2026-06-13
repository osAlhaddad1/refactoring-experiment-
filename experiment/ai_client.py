"""Talks to the AI model through OpenRouter (an OpenAI-compatible gateway), plus
a mock client for testing the pipeline without spending credits.

The real client reads the API key from OPENROUTER_API_KEY and the model slug
from OPENROUTER_MODEL (for example "google/gemini-2.5-flash"). If either is
missing it prints a clear error and stops.

Every client returns the same dict:
    {"text": <model output>, "input_tokens": int, "output_tokens": int, "latency_ms": int}
"""

import os
import sys
import time

OPENROUTER_URL = "https://openrouter.ai/api/v1/chat/completions"


def call_openrouter(prompt):
    """Calls OpenRouter's chat-completions endpoint at temperature 0."""
    import requests  # imported here so the mock works even without requests installed

    api_key = os.environ.get("OPENROUTER_API_KEY")
    model = os.environ.get("OPENROUTER_MODEL")
    if not api_key or not model:
        print("ERROR: set the OPENROUTER_API_KEY and OPENROUTER_MODEL environment variables.")
        sys.exit(1)

    headers = {"Authorization": "Bearer " + api_key}
    body = {
        "model": model,
        "messages": [{"role": "user", "content": prompt}],
        "temperature": 0,
    }

    # retry on rate limits (429), transient server errors (5xx), and network errors
    attempt = 0
    while True:
        attempt = attempt + 1
        start = time.time()
        try:
            response = requests.post(OPENROUTER_URL, headers=headers, json=body, timeout=300)
        except requests.exceptions.RequestException as error:
            if attempt <= 5:
                print("    (network problem calling OpenRouter: %s; waiting 20s then retrying)"
                      % error.__class__.__name__)
                time.sleep(20)
                continue
            raise
        latency_ms = int((time.time() - start) * 1000)
        if (response.status_code == 429 or response.status_code >= 500) and attempt <= 4:
            print("    (OpenRouter returned HTTP %d; waiting 20s then retrying)"
                  % response.status_code)
            time.sleep(20)
            continue
        break
    response.raise_for_status()
    data = response.json()

    text = ""
    choices = data.get("choices", [])
    if choices:
        text = choices[0].get("message", {}).get("content", "") or ""

    usage = data.get("usage", {})
    return {
        "text": text,
        "input_tokens": usage.get("prompt_tokens", 0),
        "output_tokens": usage.get("completion_tokens", 0),
        "latency_ms": latency_ms,
    }


def call_mock(prompt, mock_file):
    """Returns a hardcoded JSON response read from mock_file, with fake token
    counts (so the pipeline can be tested without spending credits)."""
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
