import { SERVER_ROUTES } from '$lib/config/paths.ts';
import { toast } from 'svelte-sonner';
import { browser } from '$app/environment';

type EventType = 'INITIALIZED' | 'UPDATED';

interface MonitoringHandlers {
	onInitialized: (data: DashboardSubscriptionResponse) => void;
	onUpdate: (data: DashboardSubscriptionResponse) => void;
	onError: (message: string) => void;
}

export class MonitoringService {
	private static readonly EVENTS: EventType[] = ['INITIALIZED', 'UPDATED'];

	/**
	 * Subscribes to a dashboard and returns a function to unsubscribe from the dashboard.
	 *
	 * @param dashboard The dashboard to subscribe to
	 * @param handlers The handlers for the monitoring events
	 * @param window The window for the monitoring events
	 */
	public static subscribeToDashboard(dashboard: DashboardResponse, handlers: MonitoringHandlers, window?: number): () => void {
		if (!browser) return () => {};

		const params = new URLSearchParams(window ? { window: window.toString() } : {});
		const url = `${SERVER_ROUTES.MONITORING.path.replace('{id}', dashboard.id.toString())}?${params}`;

		console.log('Connecting to SSE endpoint:', url);
		const eventSource = new EventSource(url, { withCredentials: true });

		const handleEvent = this.createEventHandler(handlers, dashboard.name);
		const handleError = this.createErrorHandler(handlers, () => {
			setTimeout(() => this.subscribeToDashboard(dashboard, handlers, window), 30000);
		});

		this.addEventListeners(eventSource, handleEvent, handleError);
		return () => {
			this.removeEventListeners(eventSource, handleEvent, handleError);
			eventSource.close();
		};
	}

	private static createEventHandler(handlers: MonitoringHandlers, name: string): (event: MessageEvent) => void {
		return (event: MessageEvent) => {
			try {
				const data = JSON.parse(event.data) as DashboardSubscriptionResponse;
				if (event.type === 'INITIALIZED') {
					handlers.onInitialized(data);
					toast.success(`Subscribed to ${name}`);
				} else {
					handlers.onUpdate(data);
				}
			} catch (error) {
				handlers.onError('Invalid data format received');
			}
		};
	}

	private static createErrorHandler(handlers: MonitoringHandlers, reconnect: () => void): () => void {
		return () => {
			handlers.onError('Connection interrupted - reconnecting...');
			reconnect();
		};
	}

	private static addEventListeners(eventSource: EventSource, handleEvent: (event: MessageEvent) => void, handleError: () => void): void {
		this.EVENTS.forEach((event) => eventSource.addEventListener(event, handleEvent));
		eventSource.addEventListener('error', handleError);
	}

	private static removeEventListeners(eventSource: EventSource, handleEvent: (event: MessageEvent) => void, handleError: () => void): void {
		this.EVENTS.forEach((event) => eventSource.removeEventListener(event, handleEvent));
		eventSource.removeEventListener('error', handleError);
	}
}
