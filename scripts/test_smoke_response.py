import unittest

from smoke_response import validate_response


class SmokeResponseTest(unittest.TestCase):
    def test_missing_help_is_not_success_even_when_it_echoes_the_command(self):
        with self.assertRaises(RuntimeError):
            validate_response("help lkjmcsmp:tp", "No help for lkjmcsmp:tp", "lkjmcsmp:tp")

    def test_formatting_does_not_hide_a_failure(self):
        for response in (
            "\x1b[31;1mNo help for lkjmcsmp:tp\x1b[0m",
            "§cNo §lhelp §rfor lkjmcsmp:tp",
            "\n NO   HELP\nFOR lkjmcsmp:tp ",
        ):
            with self.subTest(response=response), self.assertRaises(RuntimeError):
                validate_response("help lkjmcsmp:tp", response, "lkjmcsmp:tp")

    def test_unknown_denied_and_internal_errors_fail(self):
        for response in (
            "Unknown command: menu",
            "Unknown or incomplete command, see below for error: menu",
            "An internal error occurred while attempting to perform this command: menu",
            "You do not have permission to use menu",
            "You don't have permission to use menu",
        ):
            with self.subTest(response=response), self.assertRaises(RuntimeError):
                validate_response("menu", response, "menu")

    def test_empty_and_formatting_only_responses_fail(self):
        for response in ("", " \n ", "\x1b[0m§c"):
            with self.subTest(response=response), self.assertRaises(RuntimeError):
                validate_response("menu", response)

    def test_missing_positive_marker_fails(self):
        with self.assertRaises(RuntimeError):
            validate_response("plugins", "Server Plugins (0)", "lkjmcsmp")

    def test_formatted_positive_help_succeeds(self):
        validate_response("help menu", "\x1b[33mHelp: §a/menu\x1b[0m", "menu")

    def test_player_only_guard_proves_namespaced_console_dispatch(self):
        validate_response(
            "lkjmcsmp:tp", "This command can only be used by players.\n\x1b[0m",
            "This command can only be used by players.",
        )


if __name__ == "__main__":
    unittest.main()
