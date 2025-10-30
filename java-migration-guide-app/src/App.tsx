import { useCallback, useEffect, useMemo, useState } from 'react';
import {
  AppShell,
  Box,
  Button,
  Checkbox,
  Divider,
  FileInput,
  Group,
  Loader,
  ScrollArea,
  Stack,
  Text,
  TextInput,
  Textarea,
  Title,
  ActionIcon,
} from '@mantine/core';
import { IconArrowLeft, IconArrowRight, IconSearch, IconTrash, IconUpload, IconWorld } from '@tabler/icons-react';
import { PdfViewer, type PdfFile } from './components/PdfViewer';
import './App.css';

type ChecklistItem = { id: string; text: string; done: boolean };

const DEFAULT_CHECKLIST: ChecklistItem[] = [
  { id: 'inventory', text: 'Inventory current Java apps and versions', done: false },
  { id: 'deps', text: 'Audit third‑party dependencies for compatibility', done: false },
  { id: 'build', text: 'Set up modern build toolchain', done: false },
  { id: 'tests', text: 'Add/upgrade automated tests', done: false },
  { id: 'deploy', text: 'Validate deployment and rollback plan', done: false },
];

function loadFromStorage<T>(key: string, fallback: T): T {
  try {
    const raw = localStorage.getItem(key);
    return raw ? (JSON.parse(raw) as T) : fallback;
  } catch {
    return fallback;
  }
}

function saveToStorage<T>(key: string, value: T) {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch {
    // ignore
  }
}

function getDocIdFromFileSrc(src: { file?: File | null; url?: string | null }): string | null {
  if (src.file) return `${src.file.name}:${src.file.size}`;
  if (src.url) return src.url;
  return null;
}

export default function App() {
  const [fileSrc, setFileSrc] = useState<{ file?: File | null; url?: string | null }>({});
  const [pdfFile, setPdfFile] = useState<PdfFile>(null);
  const [docId, setDocId] = useState<string | null>(null);
  const [numPages, setNumPages] = useState<number>(0);
  const [pageNumber, setPageNumber] = useState<number>(1);
  const [loadingIndex, setLoadingIndex] = useState<boolean>(false);
  const [pageTexts, setPageTexts] = useState<string[] | null>(null);
  const [query, setQuery] = useState<string>('');
  const [searchResults, setSearchResults] = useState<{ page: number; count: number; snippet: string }[]>([]);
  const [notes, setNotes] = useState<Record<number, string>>({});
  const [checklist, setChecklist] = useState<ChecklistItem[]>(DEFAULT_CHECKLIST);

  // Compute and persist ids
  useEffect(() => {
    const id = getDocIdFromFileSrc(fileSrc);
    setDocId(id);
  }, [fileSrc]);

  // Load persisted notes/checklist when doc changes
  useEffect(() => {
    if (!docId) return;
    setNotes(loadFromStorage<Record<number, string>>(`notes:${docId}`, {}));
    setChecklist(loadFromStorage<ChecklistItem[]>(`checklist:${docId}`, DEFAULT_CHECKLIST));
  }, [docId]);

  useEffect(() => {
    if (!docId) return;
    saveToStorage(`notes:${docId}`, notes);
  }, [docId, notes]);

  useEffect(() => {
    if (!docId) return;
    saveToStorage(`checklist:${docId}`, checklist);
  }, [docId, checklist]);

  const handleFileUpload = (f: File | null) => {
    if (!f) return;
    const url = URL.createObjectURL(f);
    setFileSrc({ file: f });
    setPdfFile(url);
    setPageNumber(1);
    setPageTexts(null);
    setSearchResults([]);
    setQuery('');
  };

  const handleUrlLoad = () => {
    if (!urlInput.trim()) return;
    setFileSrc({ url: urlInput.trim() });
    setPdfFile(urlInput.trim());
    setPageNumber(1);
    setPageTexts(null);
    setSearchResults([]);
    setQuery('');
  };

  const [urlInput, setUrlInput] = useState('');

  const onDocumentLoad = useCallback(async ({ numPages: n, pdf }: { numPages: number; pdf: any }) => {
    setNumPages(n);
    setLoadingIndex(true);
    try {
      const texts: string[] = [];
      for (let i = 1; i <= n; i += 1) {
        // eslint-disable-next-line no-await-in-loop
        const page = await pdf.getPage(i);
        // eslint-disable-next-line no-await-in-loop
        const content = await page.getTextContent();
        const strings = content.items.map((it: any) => (typeof it.str === 'string' ? it.str : '')).join(' ');
        texts.push(strings);
      }
      setPageTexts(texts);
    } catch (e) {
      // ignore indexing failure; viewer will still work
    } finally {
      setLoadingIndex(false);
    }
  }, []);

  const doSearch = useCallback(() => {
    if (!pageTexts || !query.trim()) {
      setSearchResults([]);
      return;
    }
    const q = query.trim().toLowerCase();
    const results: { page: number; count: number; snippet: string }[] = [];
    pageTexts.forEach((text, idx) => {
      const lc = text.toLowerCase();
      const count = lc.split(q).length - 1;
      if (count > 0) {
        const firstIdx = lc.indexOf(q);
        const start = Math.max(0, firstIdx - 60);
        const end = Math.min(text.length, firstIdx + q.length + 60);
        const snippet = `${text.slice(start, firstIdx)}` +
          `[${text.slice(firstIdx, firstIdx + q.length)}]` +
          `${text.slice(firstIdx + q.length, end)}`;
        results.push({ page: idx + 1, count, snippet });
      }
    });
    results.sort((a, b) => b.count - a.count);
    setSearchResults(results.slice(0, 50));
  }, [pageTexts, query]);

  useEffect(() => {
    const t = setTimeout(() => doSearch(), 250);
    return () => clearTimeout(t);
  }, [query, doSearch]);

  const progressPct = useMemo(() => {
    if (checklist.length === 0) return 0;
    const done = checklist.filter((c) => c.done).length;
    return Math.round((done / checklist.length) * 100);
  }, [checklist]);

  return (
    <AppShell
      padding="md"
      header={{ height: 56 }}
      navbar={{ width: 360, breakpoint: 'sm' }}
    >
      <AppShell.Header>
        <Group justify="space-between" px="md" h="100%">
          <Title order={4}>Java Migration Guide App</Title>
          <Group gap="xs">
            <Button size="xs" variant="light" onClick={() => setPageNumber((p) => Math.max(1, p - 1))} leftSection={<IconArrowLeft size={16} />}>
              Prev
            </Button>
            <Text size="sm">Page {pageNumber}{numPages ? ` / ${numPages}` : ''}</Text>
            <Button size="xs" variant="light" onClick={() => setPageNumber((p) => Math.min(numPages || p + 1, p + 1))} rightSection={<IconArrowRight size={16} />}>
              Next
            </Button>
          </Group>
        </Group>
      </AppShell.Header>
      <AppShell.Navbar p="md">
        <ScrollArea style={{ height: '100%' }}>
          <Stack gap="md">
            <Box>
              <Title order={6}>Source</Title>
              <FileInput
                leftSection={<IconUpload size={16} />}
                placeholder="Upload PDF"
                accept="application/pdf"
                onChange={handleFileUpload}
              />
              <Group mt="xs" gap="xs">
                <TextInput
                  placeholder="https://...pdf"
                  value={urlInput}
                  onChange={(e) => setUrlInput(e.currentTarget.value)}
                  leftSection={<IconWorld size={16} />}
                  flex={1}
                />
                <Button onClick={handleUrlLoad}>Load</Button>
              </Group>
            </Box>

            <Divider label="Search" />
            <Box>
              <TextInput
                placeholder={loadingIndex ? 'Indexing PDF…' : 'Search text'}
                leftSection={<IconSearch size={16} />}
                value={query}
                disabled={!pageTexts}
                onChange={(e) => setQuery(e.currentTarget.value)}
              />
              {loadingIndex && (
                <Group gap="xs" mt="xs"><Loader size="xs" /><Text size="xs" c="dimmed">Building search index…</Text></Group>
              )}
              <ScrollArea h={200} mt="xs">
                <Stack gap="xs">
                  {searchResults.map((r) => (
                    <Box
                      key={`${r.page}-${r.count}-${r.snippet.slice(0, 20)}`}
                      onClick={() => setPageNumber(r.page)}
                      style={{ cursor: 'pointer' }}
                    >
                      <Text size="xs" c="dimmed">Page {r.page} · {r.count} hit{r.count > 1 ? 's' : ''}</Text>
                      <Text size="sm" style={{ whiteSpace: 'pre-wrap' }}>{r.snippet}</Text>
                    </Box>
                  ))}
                  {!loadingIndex && pageTexts && searchResults.length === 0 && (
                    <Text size="xs" c="dimmed">No results</Text>
                  )}
                </Stack>
              </ScrollArea>
            </Box>

            <Divider label={`Checklist (${progressPct}%)`} />
            <Stack gap={6}>
              {checklist.map((item) => (
                <Group key={item.id} justify="space-between" wrap="nowrap">
                  <Checkbox
                    checked={item.done}
                    onChange={(e) =>
                      setChecklist((prev) => prev.map((c) => (c.id === item.id ? { ...c, done: e.currentTarget.checked } : c)))
                    }
                    label={<Text size="sm">{item.text}</Text>}
                    styles={{ label: { cursor: 'pointer' } }}
                  />
                  <ActionIcon variant="subtle" color="red" onClick={() => setChecklist((prev) => prev.filter((c) => c.id !== item.id))}>
                    <IconTrash size={16} />
                  </ActionIcon>
                </Group>
              ))}
              <Group>
                <TextInput placeholder="Add item" flex={1} onKeyDown={(e) => {
                  if (e.key === 'Enter') {
                    const val = (e.target as HTMLInputElement).value.trim();
                    if (val) {
                      setChecklist((prev) => [...prev, { id: `${Date.now()}`, text: val, done: false }]);
                      (e.target as HTMLInputElement).value = '';
                    }
                  }
                }} />
              </Group>
            </Stack>

            <Divider label={docId ? `Notes for page ${pageNumber}` : 'Notes'} />
            <Textarea
              minRows={6}
              autosize
              placeholder={docId ? 'Write notes for this page…' : 'Open a PDF to take notes'}
              disabled={!docId}
              value={notes[pageNumber] ?? ''}
              onChange={(e) => {
                const v = e.currentTarget.value;
                setNotes((prev) => ({ ...prev, [pageNumber]: v }));
              }}
            />
          </Stack>
        </ScrollArea>
      </AppShell.Navbar>

      <AppShell.Main>
        <Box style={{ height: 'calc(100dvh - 56px - 24px)', overflow: 'hidden' }}>
          <PdfViewer file={pdfFile} pageNumber={pageNumber} onDocumentLoad={onDocumentLoad} />
        </Box>
      </AppShell.Main>
    </AppShell>
  );
}
