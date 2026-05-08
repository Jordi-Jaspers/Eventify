/**
 * Shared 12-column grid layout for the sessions table.
 * Header (SessionsTable) and row (SessionRow) must stay in sync — change here only.
 */
export const SESSION_GRID_ROW: string = 'grid grid-cols-2 md:grid-cols-12 gap-4';
export const SESSION_GRID_HEADER: string = 'hidden md:grid md:grid-cols-12 gap-4';

export const SESSION_COL: {
    device: string;
    ip: string;
    lastActive: string;
    expires: string;
    status: string;
    action: string;
} = {
    device: 'col-span-2 md:col-span-3',
    ip: 'col-span-1 md:col-span-2',
    lastActive: 'col-span-1 md:col-span-2',
    expires: 'col-span-1 md:col-span-3',
    status: 'col-span-1',
    action: 'col-span-1'
};
