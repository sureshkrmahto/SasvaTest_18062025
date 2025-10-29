import { useEffect, useMemo, useRef, useState } from 'react';
import { Document, Page, pdfjs } from 'react-pdf';

pdfjs.GlobalWorkerOptions.workerSrc = new URL('pdfjs-dist/build/pdf.worker.min.js', import.meta.url).toString();

export type PdfFile = string | ArrayBuffer | Uint8Array | null;

export function PdfViewer(props: {
  file: PdfFile;
  pageNumber: number;
  onDocumentLoad: (info: { numPages: number; pdf: any }) => void;
}) {
  const { file, pageNumber, onDocumentLoad } = props;
  const [numPages, setNumPages] = useState<number>(0);
  const containerRef = useRef<HTMLDivElement | null>(null);

  // Compute a responsive width for the page
  const pageWidth = useMemo(() => {
    const base = containerRef.current?.clientWidth ?? 800;
    return Math.min(base, 1200);
  }, [containerRef.current?.clientWidth]);

  useEffect(() => {
    const handleResize = () => {
      // Trigger rerender by changing state
      setNumPages((n) => n);
    };
    window.addEventListener('resize', handleResize);
    return () => window.removeEventListener('resize', handleResize);
  }, []);

  return (
    <div ref={containerRef} style={{ width: '100%', height: '100%', overflow: 'auto' }}>
      {file ? (
        <Document
          file={file as any}
          onLoadSuccess={(pdf) => {
            setNumPages(pdf.numPages);
            onDocumentLoad({ numPages: pdf.numPages, pdf });
          }}
          loading={<div style={{ padding: 16 }}>Loading PDF…</div>}
          error={<div style={{ padding: 16, color: 'red' }}>Failed to load PDF.</div>}
        >
          <Page pageNumber={pageNumber} width={pageWidth} renderTextLayer renderAnnotationLayer />
          <div style={{ padding: '8px 0', color: '#666' }}>Page {pageNumber} of {numPages}</div>
        </Document>
      ) : (
        <div style={{ padding: 24, color: '#666' }}>Upload a PDF or enter a URL to begin.</div>
      )}
    </div>
  );
}
