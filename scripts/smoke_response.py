"""Validate English RCON smoke responses without requiring a running server."""
import re

ANSI = re.compile(r"\x1b\[[0-?]*[ -/]*[@-~]")
LEGACY = re.compile(r"§[0-9a-fk-orx]", re.IGNORECASE)
FAILURES = (
    "no help for",
    "unknown command",
    "unknown or incomplete command",
    "an internal error occurred",
    "you do not have permission",
    "you don't have permission",
)


def normalize(text: str) -> str:
    return " ".join(LEGACY.sub("", ANSI.sub("", text)).split()).casefold()


def validate_response(command: str, response: str, must_contain: str | None = None) -> None:
    text = normalize(response)
    if not text or any(marker in text for marker in FAILURES):
        raise RuntimeError(f"command `{command}` returned a failure: `{response[:240]}`")
    if must_contain and normalize(must_contain) not in text:
        raise RuntimeError(f"command `{command}` missing `{must_contain}` in `{response[:240]}`")
