<div class="relative flex justify-center">
	<svg xmlns="http://www.w3.org/2000/svg" class="heartbeat-container" viewBox="0 0 300 150" preserveAspectRatio="xMidYMid meet">
		<polyline
			class="heartbeat"
			fill="none"
			stroke="green"
			stroke-width="2"
			stroke-miterlimit="10"
			points="0,90 77.028,90 89.19,66.648 101.352,90 115.542,90 125.676,111.244 143.918,18 160.134,127.458 168.244,90 194.594,90 206.758,80.838 220.946,90 300,90"
		/>
	</svg>
</div>

<style>
	.heartbeat-container {
		width: 100%; /* Make it responsive to the container's width */
		height: auto; /* Maintain aspect ratio */
		max-width: 800px; /* Optional: set a maximum width */
		filter: blur(1px); /* Adds subtle blur */
		opacity: 0.7; /* Reduces the opacity */
		z-index: -1;
	}

	.heartbeat {
		stroke-dasharray: 600; /* Total length of the path */
		stroke-dashoffset: 1200; /* Start hidden */
		animation: pulse 4s linear infinite;
	}

	@keyframes pulse {
		0% {
			stroke-dashoffset: 1200; /* Fully hidden */
		}
		50% {
			stroke-dashoffset: 600; /* Pulse reaches mid-point */
		}
		100% {
			stroke-dashoffset: 0; /* Pulse completes, restarts seamlessly */
		}
	}
</style>
