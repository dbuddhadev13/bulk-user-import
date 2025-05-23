import axios, { AxiosError } from "axios";
import { type FunctionComponent, useCallback, useState } from "react";
import { useDropzone } from "react-dropzone";

const HomePage: FunctionComponent = () => {
	const [file, setFile] = useState<File | null>(null);
	const [message, setMessage] = useState<string | null>(null);
	const [error, setError] = useState<string | null>(null);

	const onDrop = useCallback((acceptedFiles: File[]) => {
		if (acceptedFiles.length > 0) {
			const selectedFile = acceptedFiles[0];
			if (!selectedFile.name.endsWith(".csv")) {
				setError("Only .csv files are allowed");
				setMessage(null);
				setFile(null);
				return;
			}
			if (selectedFile.size > 5 * 1024 * 1024) {
				setError("File is too large (max 5MB)");
				setMessage(null);
				setFile(null);
				return;
			}
			setFile(selectedFile);
			setMessage(null);
			setError(null);
		}
	}, []);

	const { getRootProps, getInputProps, isDragActive } = useDropzone({
		onDrop,
		accept: { "text/csv": [".csv"] },
		maxFiles: 1,
	});

	const handleSubmit = (e: React.FormEvent) => {
		e.preventDefault();
		if (!file) {
			setError("Please select a CSV file");
			return;
		}

		const formData = new FormData();
		formData.append("file", file);

		axios
			.post("/api/upload", formData, {
				headers: { "Content-Type": "multipart/form-data" },
			})
			.then(({ data }) => {
				setMessage(data);
				setFile(null);
			})
			.catch((e: AxiosError) =>
				setError(
					(e.response?.data as string) ??
						"Upload failed. Please ensure the file is a valid CSV and try again."
				)
			);
	};

	// Generates a downloadable sample CSV
	const handleDownloadSample = () => {
		const csvContent =
			"id,firstName,lastName,email\n1,Alice,Johnson,alice@example.com\n2,Bob,Smith,bob.smith@example.com";
		const blob = new Blob([csvContent], { type: "text/csv;charset=utf-8;" });
		const url = URL.createObjectURL(blob);
		const link = document.createElement("a");
		link.href = url;
		link.setAttribute("download", "sample.csv");
		document.body.appendChild(link);
		link.click();
		document.body.removeChild(link);
		URL.revokeObjectURL(url);
	};

	return (
		<div className="min-h-screen min-w-screen bg-accent flex items-center justify-center px-4">
			<div className="bg-white rounded-2xl shadow-lg p-8 w-full max-w-lg border border-gray-200">
				<h2 className="text-2xl text-black font-bold text-secondary mb-6 text-center">
					Bulk User CSV Upload
				</h2>

				<div
					{...getRootProps()}
					className={`cursor-pointer border-2 border-dashed rounded-xl p-6 text-center transition ${
						isDragActive
							? "border-primary bg-red-50"
							: "border-gray-300 hover:border-primary"
					}`}
				>
					<input {...getInputProps()} />
					<p className="mt-2 text-gray-500">
						{file
							? `Selected: ${file.name}`
							: "Drag & drop a .csv file here, or click to select"}
					</p>
				</div>

				<div className="mt-6 flex justify-between items-center">
					<button
						onClick={handleDownloadSample}
						className="bg-primary text-white font-semibold py-2 px-4 rounded-xl hover:bg-red-700 transition disabled:opacity-50"
						disabled={!!file}
					>
						Download Sample CSV
					</button>
					<form onSubmit={handleSubmit} className="flex justify-center">
						<button
							type="submit"
							disabled={!file}
							className="bg-primary text-white font-semibold py-2 px-4 rounded-xl hover:bg-red-700 transition disabled:opacity-50"
						>
							Upload CSV
						</button>
					</form>
				</div>

				{message && (
					<div className="mt-4 bg-green-50 border border-green-200 text-green-700 px-4 py-3 rounded-xl shadow-sm text-center">
						{message}
					</div>
				)}
				{error && (
					<div className="mt-4 bg-red-50 border border-red-200 text-red-700 px-4 py-3 rounded-xl shadow-sm text-center">
						{error}
					</div>
				)}
			</div>
		</div>
	);
};

export default HomePage;
