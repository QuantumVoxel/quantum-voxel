from typing import TypeVar, Iterator

T = TypeVar('T')

class JArray(T):
    def __init__(self, *args, **kwargs):
        self.length: int
        ...

    def __iter__(self) -> Iterator[T]:
        ...

    def __getitem__(self, index: int) -> T:
        ...
